/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.managers;

import de.rexlmanu.bedwars.utils.PlayerUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.List;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 06:50                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

public class SpectatorManager {

    private List<Player> spectators;
    private List<Player> playingPlayers;

    public SpectatorManager(List<Player> spectators, List<Player> playingPlayers) {
        this.spectators = spectators;
        this.playingPlayers = playingPlayers;
    }

    public boolean isSpectator(Player player) {
        return this.spectators.contains(player);
    }

    public void setAsSpectator(Player target) {
        playingPlayers.forEach(player -> player.hidePlayer(target));
        spectators.forEach(player -> {
            if (player != target) {
                player.showPlayer(target);
                target.showPlayer(player);
            }
        });

        target.getInventory().clear();
        target.getInventory().setArmorContents(null);
        target.setGameMode(GameMode.ADVENTURE);
        target.setFoodLevel(20);
        target.setHealthScale(2);
        target.setHealth(target.getMaxHealth());
        target.setAllowFlight(true);
        target.spigot().setCollidesWithEntities(false);
    }

    public void visibleSpectators() {
        PlayerUtils.parseThroughAllPlayer(player -> this.spectators.forEach(player::showPlayer));
    }
}
