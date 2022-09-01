/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.managers;

import de.rexlmanu.bedwars.database.DatabaseConnection;
import lombok.Getter;
import de.rexlmanu.bedwars.ManagerPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 28.06.2019 / 22:59                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public final class ConfigManager {

    private File file;
    @Getter
    private FileConfiguration configuration;
    private ManagerPlugin managerPlugin;
    private List<String> teams;
    @Getter
    private DatabaseConnection databaseConnection;

    public ConfigManager(ManagerPlugin managerPlugin) {
        this.file = new File(managerPlugin.getDataFolder(), "config.yml");
        if (! this.file.exists()) {
            this.teams = Arrays.asList("Blau", "Rot");
            this.createDefaultConfiguration();
        }

        this.loadConfiguration();
    }

    private void loadConfiguration() {
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
        this.databaseConnection = new DatabaseConnection(
                this.configuration.getString("username"),
                this.configuration.getString("hostname"),
                this.configuration.getString("database"),
                this.configuration.getString("password"),
                this.configuration.getInt("port")
        );
    }

    private void createDefaultConfiguration() {
        try {
            this.file.createNewFile();
            this.configuration = YamlConfiguration.loadConfiguration(this.file);
            this.configuration.set("teamNames", teams);
            this.configuration.set("teamSize", 1);
            this.configuration.set("teams", 2);
            this.configuration.set("minPlayers", 2);
            this.configuration.set("offSet", 2);
            this.configuration.set("prefix", "§8» §9BedWars §8× §r");
            this.configuration.set("database", "network");
            this.configuration.set("hostname", "localhost");
            this.configuration.set("username", "admin");
            this.configuration.set("password", "sad");
            this.configuration.set("port", 3306);
            this.configuration.set("lobby", null);
            this.configuration.set("hologram", null);
            this.configuration.save(this.file);
        } catch (IOException ignored) {
        }
    }

    public List<String> getTeams() {
        return this.configuration.getStringList("teamNames");
    }

    public void save() {
        try {
            this.configuration.save(this.file);
        } catch (IOException ignored) {
        }
    }
}
