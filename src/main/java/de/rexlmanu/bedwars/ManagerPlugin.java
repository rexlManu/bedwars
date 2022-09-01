/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars;

import de.rexlmanu.bedwars.command.StatsCommand;
import de.rexlmanu.bedwars.database.DatabaseManager;
import de.rexlmanu.bedwars.managers.*;
import de.rexlmanu.bedwars.misc.StatsWallTask;
import de.rexlmanu.bedwars.misc.npc.NPCManager;
import de.rexlmanu.bedwars.utils.LocaleQuery;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 04:35                           
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
public class ManagerPlugin extends JavaPlugin {

    private EventManager eventManager;
    private GameManager gameManager;
    private SettingsManager settingsManager;
    private ArenaManager arenaManager;
    private TeamManager teamManager;
    private ConfigManager configManager;
    private ShopManager shopManager;
    private ScoreboardManager scoreboardManager;
    private DatabaseManager databaseManager;
    private StatsManager statsManager;
    private NPCManager npcManager;
    private LocaleQuery localeQuery;
    private StatsWallTask statsWallTask;

    void init() {
        this.getDataFolder().mkdir();
        File file = new File("./Wartelobby");
        if (file.exists() && file.isDirectory()) {
            Bukkit.createWorld(WorldCreator.name("Wartelobby"));
        }

        this.configManager = new ConfigManager(this);
        this.arenaManager = new ArenaManager(this);
        this.eventManager = new EventManager(this);
        this.teamManager = new TeamManager(this);
        this.settingsManager = new SettingsManager(this);
        this.gameManager = new GameManager();
        this.shopManager = new ShopManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.databaseManager = new DatabaseManager(this.configManager.getDatabaseConnection());
        this.statsManager = new StatsManager(this);
        this.npcManager = new NPCManager(this);
        this.localeQuery = new LocaleQuery();
        this.statsWallTask = new StatsWallTask(this);

        this.databaseManager.openConnection(() ->
                this.databaseManager.update("CREATE TABLE IF NOT EXISTS `bwStats` ( `uuid` VARCHAR(36) NOT NULL , `kills` INT NOT NULL DEFAULT '0' , `deaths` INT NOT NULL DEFAULT '0' , `games` INT NOT NULL DEFAULT '0' , `wins` INT NOT NULL DEFAULT '0' , `beds` INT NOT NULL DEFAULT '0' ) ENGINE = InnoDB;"));

        this.configManager.getTeams().forEach(teamDisplayName -> this.teamManager.registerTeam(teamDisplayName));

        this.arenaManager.loadArenas();

        this.npcManager.runTaskTimerAsynchronously(this, 0, 1);
        new Weather().runTaskTimer(this, 0, 20 * 60);
        this.statsWallTask.runTaskTimer(this, 1, 20 * 60 * 60);

        getCommand("stats").setExecutor(new StatsCommand(this));
    }

    public BukkitTask schedule(final long delay, final long period, final Runnable callback) {
        return this.getServer().getScheduler().runTaskTimer(this, callback, delay, period);
    }

    public BukkitTask runLater(final long delay, final Runnable runnable) {
        return this.getServer().getScheduler().runTaskLater(this, runnable, delay);
    }

    public BukkitTask runTask(final Runnable runnable) {
        return this.getServer().getScheduler().runTask(this, runnable);
    }

    public BukkitTask runTaskAsync(final long delay, final long period, final Runnable runnable) {
        return this.getServer().getScheduler().runTaskTimerAsynchronously(this, runnable, delay, period);
    }

    public BukkitTask runTaskLaterAsync(final long delay, final Runnable runnable) {
        return this.getServer().getScheduler().runTaskLaterAsynchronously(this, runnable, delay);
    }

    public void async(final Runnable run) {
        this.getServer().getScheduler().runTaskAsynchronously(this, run);
    }

    public void sync(final Runnable run) {
        this.getServer().getScheduler().runTask(this, run);
    }

    class Weather extends BukkitRunnable {

        @Override
        public void run() {
            for (final World world : Bukkit.getWorlds()) {
                world.setTime(0);
                world.setStorm(false);
                world.setWeatherDuration(0);
                world.setThunderDuration(0);
            }
        }
    }
}
