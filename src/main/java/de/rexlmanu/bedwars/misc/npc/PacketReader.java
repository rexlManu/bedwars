/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.misc.npc;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 14.07.2018 / 12:30                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.Packet;
import de.rexlmanu.bedwars.ManagerPlugin;
import de.rexlmanu.bedwars.gamestates.states.IngameState;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketReader {

    @Getter
    private static Map<Player, PacketReader> packetReaderMap;

    static {
        PacketReader.packetReaderMap = new HashMap<>();
    }

    private final Player player;
    private Channel channel;
    private final ManagerPlugin plugin;

    public PacketReader(final Player player, final ManagerPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void inject() {
        final CraftPlayer cPlayer = (CraftPlayer) this.player;
        this.channel = cPlayer.getHandle().playerConnection.networkManager.channel;
        this.channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Packet<?>>() {
            @Override
            protected void decode(final ChannelHandlerContext arg0, final Packet<?> packet, final List<Object> arg2) throws Exception {
                arg2.add(packet);
                PacketReader.this.readPacket(packet);
            }
        });
    }

    public void uninject() {
        if (this.channel.pipeline().get("PacketInjector") != null) {
            this.channel.pipeline().remove("PacketInjector");
        }
    }


    public void readPacket(final Packet<?> packet) {
        if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")) {
            final int id = (Integer) this.getValue(packet, "a");
            if (this.plugin.getNpcManager().getPlayerNPCMap().containsKey(this.player)) {
                for (final NPC npc : this.plugin.getNpcManager().getPlayerNPCMap().get(this.player)) {
                    if (npc.getEntityID() == id) {
                        final String action = this.getValue(packet, "action").toString();
                        if (action.equalsIgnoreCase("ATTACK")) {
                            npc.playAnimation(1);
                        } else if (action.equalsIgnoreCase("INTERACT_AT")) {
                            if (! IngameState.getSpectatorManager().isSpectator(this.player)) {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                                    this.plugin.getShopManager().openShop(player, null, null);
                                    PacketReader.this.player.playSound(PacketReader.this.player.getLocation(), Sound.NOTE_PLING, 1f, 3f);
                                }, 0);
                            }
                        }
                    }

                }

            }


//            if(Main.npc.getEntityID() == id){
//                if(getValue(packet, "action").toString().equalsIgnoreCase("ATTACK")){
//                    //Main.npc.animation(1);
//                }else if(getValue(packet, "action").toString().equalsIgnoreCase("INTERACT")){
//                    player.openInventory(player.getEnderChest());
//                }
//            }

        }
    }


    public void setValue(final Object obj, final String name, final Object value) {
        try {
            final Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (final Exception e) {
        }
    }

    public Object getValue(final Object obj, final String name) {
        try {
            final Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(obj);
        } catch (final Exception e) {
        }
        return null;
    }

}