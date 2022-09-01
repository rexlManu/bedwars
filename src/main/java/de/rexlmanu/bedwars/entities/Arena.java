/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.entities;

import com.google.common.collect.Lists;
import de.rexlmanu.bedwars.managers.SpawnerManager;
import de.rexlmanu.bedwars.misc.Region;
import lombok.Data;
import de.rexlmanu.bedwars.ManagerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 01.09.2019 / 05:02                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

@Data
public final class Arena implements Comparable<Arena> {

    public static Arena createNewMap(ManagerPlugin plugin, String name, String builder) {
        File file = new File(plugin.getArenaManager().getMapDirectory().toFile(), name + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.set("name", name);
        configuration.set("builder", builder);
        configuration.set("spawners", new ArrayList<String>());
        try {
            configuration.save(file);
        } catch (IOException e) {
        }
        return new Arena(plugin, file);
    }

    private ManagerPlugin managerPlugin;
    private File file;
    private FileConfiguration configuration;
    private String name, builder;
    private List<Player> voters;
    private Map<SpawnerManager.SpawnerType, List<Location>> spawners;
    private SpawnerManager spawnerManager;

    private Location position1, position2, spectator;
    private Region region;


    public Arena(ManagerPlugin managerPlugin, File file) {
        this.managerPlugin = managerPlugin;
        this.file = file;
        Bukkit.createWorld(WorldCreator.name(file.getName().replace(".yml", "")));
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
        this.voters = new LinkedList<>();
        this.name = this.configuration.getString("name");
        this.builder = this.configuration.getString("builder");
        this.spawners = this.loadSpawners();
        this.spawnerManager = new SpawnerManager(spawners, this.managerPlugin);
        this.position1 = this.getLocation("pos1");
        this.position2 = this.getLocation("pos2");
        this.spectator = this.getLocation("spectator");
        if (this.position1 != null && this.position2 != null)
            this.region = new Region(position1, position2);
    }

    public boolean isInArena(Location location) {
        return this.region.locationIsInRegion(location);
    }

    private Map<SpawnerManager.SpawnerType, List<Location>> loadSpawners() {
        Map<SpawnerManager.SpawnerType, List<Location>> spawnerTypeListHashMap = new HashMap<>();
        for (SpawnerManager.SpawnerType value : SpawnerManager.SpawnerType.values())
            spawnerTypeListHashMap.put(value, new ArrayList<>());
        this.configuration.getStringList("spawners").forEach(s -> {
            String[] data = s.split("_");
            spawnerTypeListHashMap.get(SpawnerManager.SpawnerType.valueOf(data[0])).add(new Location(Bukkit.getWorld(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3]), (Double.parseDouble(data[4]))));
        });
        return spawnerTypeListHashMap;
    }

    public void addSpawner(SpawnerManager.SpawnerType spawnerType, Location location) {
        this.spawners.get(spawnerType).add(location);
        this.saveSpawners();
    }

    private void saveSpawners() {
        List<String> spawnList = Lists.newArrayList();
        this.spawners.forEach((spawnerType, locations) -> locations.forEach(location -> {
            spawnList.add(spawnerType.name() + "_" + location.getWorld().getName() + "_" + location.getX() + "_" + location.getY() + "_" + location.getZ());
        }));
        this.configuration.set("spawners", spawnList);
        this.save();
    }

    public Location getLocation(String key) {
        return (Location) this.configuration.get(key);
    }

    public void setLocation(String key, Location location) {
        this.configuration.set(key, location);
        this.save();
    }

    private void save() {
        try {
            this.configuration.save(this.file);
        } catch (IOException ignored) {
        }
    }

    private void teleportPlayer(Player player, Location location) {
        player.teleport(location);
    }

    public void teleportPlayer(Player player, String key) {
        if (getLocation(key) == null) return;
        this.teleportPlayer(player, getLocation(key));
    }

    @Override
    public int compareTo(Arena o) {
        return Integer.compare(this.voters.size(), o.getVoters().size());
    }
}
