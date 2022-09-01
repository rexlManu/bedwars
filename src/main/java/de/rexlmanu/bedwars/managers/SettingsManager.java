/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.managers;

import lombok.Getter;
import lombok.Setter;
import de.rexlmanu.bedwars.ManagerPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 03:45                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

@Getter
public final class SettingsManager {

    private int minPlayers, maxPlayers, teamSize, teams, offSet;
    private String prefix;
    @Setter
    @Getter
    private boolean gold = true, cobweb = true;
    private ManagerPlugin plugin;
    private Location lobby, hologram;
    private List<Location> rankHeads;

    public SettingsManager(ManagerPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration configuration = this.plugin.getConfigManager().getConfiguration();
        this.teamSize = configuration.getInt("teamSize");
        this.teams = configuration.getInt("teams");
        this.minPlayers = configuration.getInt("minPlayers");
        this.maxPlayers = teams * teamSize;
        this.offSet = configuration.getInt("offSet");
        this.prefix = configuration.getString("prefix");
        this.lobby = (Location) configuration.get("lobby");
        this.hologram = (Location) configuration.get("hologram");
        this.rankHeads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (configuration.contains("ranking-" + i))
                this.rankHeads.add((Location) configuration.get("ranking-" + i));
        }
    }

    public void setLocation(String key, Location location) {
        this.plugin.getConfigManager().getConfiguration().set(key, location);
        this.plugin.getConfigManager().save();
    }

}
