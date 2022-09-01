/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.misc.npc;

import com.mojang.authlib.properties.Property;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import de.rexlmanu.bedwars.ManagerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 14.07.2018 / 11:35                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

public final class NPCManager extends BukkitRunnable {


    private final ManagerPlugin plugin;
    @Getter
    private final Map<Player, List<NPC>> playerNPCMap;

    public NPCManager(final ManagerPlugin plugin) {
        this.plugin = plugin;
        this.playerNPCMap = new HashMap<>();
    }

    public void spawnNPC(final Player player, final Location npcLocation) {
        if (!this.playerNPCMap.containsKey(player)) {
            this.playerNPCMap.put(player, new ArrayList<>());
        }
        final String name = "§9Shop§" + new Random().nextInt(6);
        final NPC npc = new NPC(name, UUID.randomUUID(), npcLocation, player);
        final Property textures = ((CraftPlayer) player).getProfile().getProperties().get("textures").iterator().next();
        npc.changeSkin(textures.getValue(), textures.getSignature());
        npc.spawn();
        npc.headRotation(npcLocation.getYaw(), npcLocation.getPitch());
        npc.teleport(npcLocation);
        this.playerNPCMap.get(player).add(npc);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, npc::removeFromTablist, 20);
    }

    @Override
    public void run() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (this.playerNPCMap.containsKey(player)) {
                for (final NPC npc : this.playerNPCMap.get(player)) {
                    if (player.getLocation().getWorld() == npc.getLocation().getWorld()) {
                        if (player.getLocation().distance(npc.getLocation()) < 5) {
                            final PitchYaw pitchYaw = this.getLookFromPitch(player.getLocation(), npc.getLocation());
                            npc.headRotation(pitchYaw.getYaw(), pitchYaw.getPitch());
                            npc.setSneak(player.isSneaking());
                            npc.updateAction();
                        } else {
                            if (npc.isSneak()) {
                                npc.setSneak(false);
                                npc.updateAction();
                            }
                        }
                    }
                }
            }
        }
    }

    private PitchYaw getLookFromPitch(final Location to, final Location fromLocation) {
        final double xDiff;
        final double yDiff;
        final double zDiff;
        xDiff = to.getX() - fromLocation.getX();
        yDiff = to.getY() - fromLocation.getY();
        zDiff = to.getZ() - fromLocation.getZ();

        final double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        final double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ));
        final double pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 90;
        if (zDiff < 0.0) {
            yaw += Math.abs(180 - yaw) * 2;
        }

        yaw = yaw - 90;

        return new PitchYaw((float) pitch, (float) yaw);
    }

    @Data
    @AllArgsConstructor
    class PitchYaw {


        private float pitch;
        private float yaw;
    }
}
