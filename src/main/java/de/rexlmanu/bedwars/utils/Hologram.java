/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.WorldServer;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 01.09.2019 / 03:35                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public final class Hologram {

    private Location location;
    private List<String> lines;
    private double distance_above = -0.27D;
    private List<EntityArmorStand> armorstands = new ArrayList();
    private ConcurrentHashMap<Player, Hologram> hologram = new ConcurrentHashMap<>();

    public Hologram(Location loc, String... lines) {
        this.location = loc;
        this.lines = Arrays.asList(lines);
    }

    public Hologram(Location loc, List<String> lines) {
        this.location = loc;
        this.lines = ((ArrayList) lines);
    }

    public List<String> getLines() {
        return this.lines;
    }

    public Location getLocation() {
        return this.location;
    }

    public void send(Player p) {
        double y = getLocation().getY();
        for (int i = 0; i <= this.lines.size() - 1; i++) {
            y += this.distance_above;
            EntityArmorStand eas = getEntityArmorStand(y);
            eas.setCustomName((String) this.lines.get(i));
            display(p, eas);
            this.armorstands.add(eas);
        }
        hologram.put(p, this);
    }

    public void destroy(Player p) {
        for (EntityArmorStand eas : this.armorstands) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] { eas.getId() });
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
        hologram.remove(p);
    }

    public void destroy() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            destroy(p);
        }
    }

    public void broadcast() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            send(p);
        }
    }

    public void broadcast(List<Player> players) {
        for (Player p : players) {
            send(p);
        }
    }

    private EntityArmorStand getEntityArmorStand(double y) {
        WorldServer world = ((CraftWorld) getLocation().getWorld()).getHandle();
        EntityArmorStand eas = new EntityArmorStand(world);
        eas.setLocation(getLocation().getX(), y, getLocation().getZ(), 0.0F, 0.0F);
        eas.setInvisible(true);
        eas.setCustomNameVisible(true);
        return eas;
    }

    private void display(Player p, EntityArmorStand eas) {
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(eas);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

}
