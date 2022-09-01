/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.managers;

import com.google.common.collect.Lists;
import de.rexlmanu.bedwars.database.DatabaseManager;
import de.rexlmanu.bedwars.utils.Hologram;
import lombok.Getter;
import lombok.var;
import de.rexlmanu.bedwars.ManagerPlugin;
import de.rexlmanu.bedwars.entities.Stats;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 01.09.2019 / 02:35                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public final class StatsManager {

    private ManagerPlugin managerPlugin;
    @Getter
    private List<Stats> statsList;

    private List<Hologram> holograms;

    public StatsManager(ManagerPlugin managerPlugin) {
        this.managerPlugin = managerPlugin;
        this.statsList = Lists.newArrayList();
        this.holograms = Lists.newLinkedList();
    }

    public CompletableFuture<List<Stats>> getTopTenStats() {
        var future = new CompletableFuture<List<Stats>>();
        this.managerPlugin.getDatabaseManager().execute(this.managerPlugin.getDatabaseManager().prepareStatement("SELECT * FROM bwStats ORDER BY wins DESC"), (resultSet, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            try {
                List<Stats> stats = Lists.newLinkedList();
                int rank = 0;
                while (resultSet.next() && rank < 10) {
                    stats.add(new Stats(UUID.fromString(resultSet.getString("uuid")), resultSet.getInt("kills"), resultSet.getInt("deaths"), resultSet.getInt("beds"), resultSet.getInt("games"), resultSet.getInt("wins"), rank + 1, true));
                    rank++;
                }
                resultSet.close();
                future.complete(stats);
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public CompletableFuture<Integer> getRankByStats(UUID uuid) {
        var future = new CompletableFuture<Integer>();
        this.managerPlugin.getDatabaseManager().execute(this.managerPlugin.getDatabaseManager().prepareStatement("SELECT * FROM bwStats ORDER BY wins DESC"), (resultSet, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            try {
                int rank = 0;
                while (resultSet.next()) {
                    rank++;
                    if (resultSet.getString("uuid").equals(uuid.toString())) {
                        break;
                    }
                }
                resultSet.close();
                future.complete(rank);
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public CompletableFuture<Stats> loadStats(UUID uuid) {
        var future = new CompletableFuture<Stats>();
        this.managerPlugin.getDatabaseManager().execute(this.managerPlugin.getDatabaseManager().builder("SELECT * FROM bwStats WHERE uuid = ?;").bindString(uuid.toString()).build(), (resultSet, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            try {
                if (! resultSet.next()) {
                    future.complete(Stats.emptyStats(uuid));
                    return;
                }
                this.getRankByStats(uuid).whenComplete((rank, throwable1) -> {
                    try {
                        future.complete(new Stats(uuid, resultSet.getInt("kills"), resultSet.getInt("deaths"), resultSet.getInt("beds"), resultSet.getInt("games"), resultSet.getInt("wins"), rank, true));
                        resultSet.close();
                    } catch (SQLException e) {
                        future.completeExceptionally(e);
                    }
                });
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public void saveStats(Stats stats) {
        DatabaseManager databaseManager = this.managerPlugin.getDatabaseManager();
        Runnable runnable = () -> databaseManager.update(databaseManager.builder("UPDATE `bwStats` SET `kills`=?,`deaths`=?,`games`=?,`wins`=?,`beds`=? WHERE uuid = ?")
                .bindInt(stats.getKills())
                .bindInt(stats.getDeaths())
                .bindInt(stats.getGames())
                .bindInt(stats.getWins())
                .bindInt(stats.getBeds())
                .bindString(stats.getUuid().toString())
                .build());
        if (! stats.isExists())
            databaseManager.update(databaseManager.builder("INSERT INTO `bwStats`(`uuid`) VALUES (?);").bindString(stats.getUuid().toString()).build()).thenRun(runnable);
        else runnable.run();
    }

    public Stats getStatsByPlayer(UUID uuid) {
        return statsList.stream().filter(stats -> stats.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public void spawnStatsHologram(Player player, Stats stats) {

        Location location = managerPlugin.getSettingsManager().getHologram();
        if (location == null) return;
        final Hologram hologram = new Hologram(location,
                "§9BedWars §7Stats",
                "§7Position im Ranking §8» §9" + (stats.getRank() == 0 ? "?" : stats.getRank()),
                "§7Kills §8» §9" + stats.getKills(),
                "§7Tode §8» §9" + stats.getDeaths(),
                "§7K/D §8» §9" + stats.getKD(),
                "§7Gespielte Spiele §8» §9" + stats.getGames(),
                "§7Gewonnene Spiele §8» §9" + stats.getWins(),
                "§7Zerstörte Betten §8» §9" + stats.getBeds(),
                "§7Deine Siegeswahrscheinlichkeit §8» §9" + stats.winrate() + "%"
        );
        hologram.send(player);
        this.holograms.add(hologram);
    }

    public void removeHolograms() {
        this.holograms.forEach(Hologram::destroy);
        this.holograms.clear();
    }
}
