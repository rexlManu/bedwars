/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.managers;

import de.rexlmanu.bedwars.entities.Arena;
import de.rexlmanu.bedwars.misc.TeamColor;
import de.rexlmanu.bedwars.ManagerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Random;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 03.07.2019 / 17:58                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public final class ScoreboardManager {

    private ManagerPlugin plugin;

    public ScoreboardManager(ManagerPlugin managerPlugin) {
        this.plugin = managerPlugin;
    }

    public void sendRanksTablist(Player player) {
        Scoreboard scoreboard = this.getScoreboard(player);
        /*final Random random = new Random();
        for (final Player all : Bukkit.getOnlinePlayers()) {
            final CloudPlayer cloudPlayer = CloudAPI.getInstance().getOnlinePlayer(all.getUniqueId());
            final PermissionGroup highestPermissionGroup = cloudPlayer.getPermissionEntity().getHighestPermissionGroup(CloudAPI.getInstance().getPermissionPool());
            int tagId = highestPermissionGroup.getTagId();
            final String teamName = tagId + "_" + this.shortName(all.getName()) + random.nextInt(9);
            if (scoreboard.getTeam(teamName) != null) {
                scoreboard.getTeam(teamName).unregister();
            }
            final Team team = scoreboard.registerNewTeam(teamName);
            team.addEntry(all.getName());
            String suffix = " ";
            team.setPrefix(highestPermissionGroup.getPrefix());
            team.setSuffix(suffix);
        }*/
        player.setScoreboard(scoreboard);
    }

    private String shortName(final String input) {
        if (input.length() > 10) {
            return input.substring(0, 10);
        }
        return input;
    }


    public void sendTeamTablist(Player player) {
        Scoreboard scoreboard = this.getScoreboard(player);

        plugin.getTeamManager().getTeams().forEach(team -> {
            if (scoreboard.getTeam(team.getDisplayName()) != null)
                scoreboard.getTeam(team.getDisplayName()).unregister();
            TeamColor teamColor = plugin.getTeamManager().getColorByDisplayName(team.getDisplayName());
            Team scoreboardTeam = scoreboard.registerNewTeam(team.getDisplayName());
            scoreboardTeam.setPrefix("§" + teamColor.getKey() + "◆ §8︳ §7");
            scoreboardTeam.setAllowFriendlyFire(false);
            scoreboardTeam.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OWN_TEAM);
            team.getTeamMembers().forEach(o -> scoreboardTeam.addEntry(o.getName()));
        });

        player.setScoreboard(scoreboard);
    }

    public void sendLobbyScoreboard(Player player, int players) {
        Scoreboard scoreboard = this.getScoreboard(player);

        Objective objective = scoreboard.getObjective("bedwars");
        if (objective != null) objective.unregister();
        objective = scoreboard.registerNewObjective("bedwars", "dummy");

        objective.setDisplayName("§8» §9§lVenium.DE");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        String teamName = "§9Keins";
        TeamManager teamManager = plugin.getTeamManager();
        if (teamManager.getTeamByPlayer(player) != null) {
            TeamColor color = teamManager.getColorByDisplayName(teamManager.getTeamByPlayer(player).getDisplayName());
            teamName = "§" + color.getKey() + color.getDisplayName();
        }
        Arena currentArena = plugin.getArenaManager().getCurrentArena();
        this.buildSimpleScoreboard(
                objective,
                "§0",
                "§9◆ §7Team§8:",
                "  §8» §7" + teamName,
                "§1",
                "§9◆ §7Spieler§8:",
                "  §8» §9" + players,
                "§2",
                "§9◆ §7Map§8:",
                "  §8» §9" + (currentArena == null ? "???" : currentArena.getName()),
                "§3",
                "§9◆ §7Teamspeak§8:",
                "  §8» §9Venium.de",
                "§r"

        );
        player.setScoreboard(scoreboard);
    }

    private void buildSimpleScoreboard(Objective objective, String... lines) {
        for (int i = 0; i < lines.length; i++)
            objective.getScore(lines[i]).setScore(lines.length - i);
    }

    private Scoreboard getScoreboard(Player player) {
        return player.getScoreboard() == null ? Bukkit.getScoreboardManager().getNewScoreboard() : player.getScoreboard();
    }
}
