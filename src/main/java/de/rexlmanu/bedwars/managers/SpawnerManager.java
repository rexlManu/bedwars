/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import de.rexlmanu.bedwars.ManagerPlugin;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 28.06.2019 / 23:35                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public final class SpawnerManager {

    private ManagerPlugin managerPlugin;
    private List<SpawnerRunnable> spawnerRunnables;
    private Map<SpawnerType, List<Location>> spawners;

    public SpawnerManager(Map<SpawnerType, List<Location>> spawners, ManagerPlugin managerPlugin) {
        this.spawners = spawners;
        this.managerPlugin = managerPlugin;
        this.spawnerRunnables = new ArrayList<>();
        this.spawners.forEach((spawnerType, locations) ->
                this.spawnerRunnables.add(new SpawnerRunnable(spawnerType, locations, managerPlugin)));
    }

    public void enableSpawners() {
        this.spawnerRunnables.forEach(SpawnerRunnable::start);
    }

    public void disableSpawners() {
        this.spawnerRunnables.forEach(SpawnerRunnable::stop);
    }

    @Getter
    public class SpawnerConfiguration {

        private Map<SpawnerType, List<Location>> spawners = new HashMap<>();

        public SpawnerConfiguration() {
            for (SpawnerType value : SpawnerType.values())
                if (! this.spawners.containsKey(value)) this.spawners.put(value, new ArrayList<>());
        }
    }

    @AllArgsConstructor
    public class SpawnerRunnable extends BukkitRunnable {

        private SpawnerType spawnerType;
        private List<Location> locations;
        private ManagerPlugin managerPlugin;

        @Override
        public void run() {
            if (spawnerType.equals(SpawnerType.GOLD) && ! managerPlugin.getSettingsManager().isGold()) return;
            this.locations.forEach(location -> {
                Item item = location.getWorld().dropItem(location, new ItemStack(spawnerType.getMaterial()));
                item.setVelocity(new Vector(0, 0.2, 0));
                location.getWorld().playEffect(location, Effect.CRIT, 1, 1);
            });
        }

        public void start() {
            this.runTaskTimer(this.managerPlugin, 0, spawnerType.getDuration());
        }

        public void stop() {
            this.cancel();
        }
    }

    @Getter
    @AllArgsConstructor
    public enum SpawnerType {

        GOLD("Gold", Material.GOLD_INGOT, 30 * 20),
        IRON("Eisen", Material.IRON_INGOT, 10 * 20),
        BRONZE("Bronze", Material.CLAY_BRICK, 10);

        private String translation;
        private Material material;
        private int duration;

    }

}
