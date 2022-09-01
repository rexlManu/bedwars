/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.command;

import de.rexlmanu.bedwars.entities.Stats;
import de.rexlmanu.bedwars.managers.StatsManager;
import de.rexlmanu.bedwars.ManagerPlugin;
import de.rexlmanu.bedwars.utils.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 01.09.2019 / 04:08                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public final class StatsCommand implements CommandExecutor {

    private final ManagerPlugin managerPlugin;

    public StatsCommand(ManagerPlugin managerPlugin) {
        this.managerPlugin = managerPlugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        String prefix = managerPlugin.getSettingsManager().getPrefix();

        if (arguments.length != 1) {
            commandSender.sendMessage(prefix + "§7/stats <Name>");
            return false;
        }

        String name = arguments[0];
        Player player = Bukkit.getPlayer(name);
        StatsManager statsManager = managerPlugin.getStatsManager();
        if (player != null) {
            printStats(statsManager.getStatsByPlayer(player.getUniqueId()), commandSender);
            return true;
        }

        UUIDFetcher.getUUID(name).thenAccept(uuid -> {
            Stats stats = statsManager.getStatsByPlayer(uuid);
            if (stats != null) {
                printStats(stats, commandSender);
                return;
            }
            statsManager.loadStats(uuid).thenAccept(loadedStats -> {
                printStats(loadedStats, commandSender);
            });
        });
        return false;
    }

    private void printStats(Stats stats, CommandSender sender) {
        sender.sendMessage("§r");
        sender.sendMessage("§8➲ §9Deine Statistiken");
        sender.sendMessage("   §7Position im Ranking §8» §9" + (stats.getRank() == 0 ? "?" : stats.getRank()));
        sender.sendMessage("   §7Kills §8» §9" + stats.getKills());
        sender.sendMessage("   §7Tode §8» §9" + stats.getDeaths());
        sender.sendMessage("   §7K/D §8» §9" + stats.getKD());
        sender.sendMessage("   §7Gespielte Spiele §8» §9" + stats.getGames());
        sender.sendMessage("   §7Gewonnene Spiele §8» §9" + stats.getWins());
        sender.sendMessage("   §7Zerstörte Betten §8» §9" + stats.getBeds());
        sender.sendMessage("   §7Deine Siegeswahrscheinlichkeit §8» §9" + stats.winrate() + "%");
        sender.sendMessage("§r");
    }
}
