/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.utils;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 05:10                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

public class PlayerUtils {

    private static final Random RANDOM = new Random();

    public static void parseThroughAllPlayer(Consumer<Player> consumer) {
        Bukkit.getOnlinePlayers().forEach(consumer::accept);
    }

    public static void broadcastMessage(String message, String permission) {
        parseThroughAllPlayer(player -> {
            if (player.hasPermission(permission)) player.sendMessage(message);
        });
    }

    public static void clearPlayer(Player player){
        player.getInventory().clear();
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setArmorContents(null);
        player.setHealthScale(20);
        player.setHealth(player.getMaxHealth());
    }

    public static void sendActionBar(final Player player, final String message) {
        final String translateAlternateColorCodes = ChatColor.translateAlternateColorCodes('&', message);
        final IChatBaseComponent chatBaseComponent = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + translateAlternateColorCodes + "\"}");
        final PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(chatBaseComponent, (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutChat);
    }

    public static Player getRandomPlayer(List<Player> playerList) {
        return playerList.get(RANDOM.nextInt(playerList.size()));
    }
}
