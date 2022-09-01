/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.misc;

import com.google.common.collect.Lists;
import lombok.Setter;
import de.rexlmanu.bedwars.ManagerPlugin;
import de.rexlmanu.bedwars.entities.Stats;
import de.rexlmanu.bedwars.utils.UUIDFetcher;
import org.bukkit.Location;
import org.bukkit.SkullType;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 02.09.2019 / 12:19                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public final class StatsWallTask extends BukkitRunnable {

    private ManagerPlugin managerPlugin;
    @Setter
    private List<Stats> best10Players;

    public StatsWallTask(ManagerPlugin managerPlugin) {
        this.managerPlugin = managerPlugin;
        this.best10Players = Lists.newArrayList();
    }

    private int i = 0;

    public void updateWall() {
        List<Location> heads = this.managerPlugin.getSettingsManager().getRankHeads();
        for (i = 0; i < heads.size(); i++) {
            Stats stats = null;
            if (best10Players.size() > i) stats = best10Players.get(i);
            Location location = heads.get(i);

            if (stats == null) {
                try{
                    Skull block = (Skull) location.getBlock().getState();
                    block.setSkullType(SkullType.PLAYER);
                    block.setOwner("MHF_Question".toLowerCase());
                    block.update(true);

                    Sign sign = (Sign) location.clone().subtract(0, 1, 0).getBlock().getState();
                    sign.setLine(0, "§9✦ §8" + ("0"));
                    sign.setLine(1, "§8" + "???");
                    sign.setLine(2, "§8" + "0" + " wins");
                    sign.setLine(3, "§8" + "0.0" + " K/D");
                    sign.update(true);
                }catch (Exception e){
                }
            } else {
                Stats finalStats = stats;
                UUIDFetcher.getName(stats.getUuid()).thenAccept(s -> {
                    Skull block = (Skull) location.getBlock().getState();
                    block.setSkullType(SkullType.PLAYER);
                    block.setOwner(s.toLowerCase());
                    block.update();

                    Sign sign = (Sign) location.clone().subtract(0, 1, 0).getBlock().getState();
                    sign.setLine(0, "§9✦ §8" + finalStats.getRank());
                    sign.setLine(1, "§8" + s);
                    sign.setLine(2, "§8" + finalStats.getWins() + " wins");
                    sign.setLine(3, "§8" + finalStats.getKD() + " K/D");
                    sign.update();
                });
            }

        }
    }

    @Override
    public void run() {
        this.managerPlugin.getStatsManager().getTopTenStats().thenAccept(stats -> {
            this.setBest10Players(stats);
            this.updateWall();
        });
    }
}
