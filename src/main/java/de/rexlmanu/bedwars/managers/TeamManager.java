/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.managers;

import de.rexlmanu.bedwars.entities.Team;
import de.rexlmanu.bedwars.ManagerPlugin;
import de.rexlmanu.bedwars.misc.TeamColor;
import de.rexlmanu.bedwars.utils.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 06:18                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

public class TeamManager {

    private ManagerPlugin plugin;
    private List<Team> teams;

    public TeamManager(ManagerPlugin plugin) {
        this.plugin = plugin;
        this.teams = new ArrayList<>();
    }

    public void registerTeam(String displayName) {
        this.teams.add(new Team(displayName, plugin));
    }

    public Team getTeamByDisplayName(String displayName) {
        for (Team team : teams) {
            if (team.getDisplayName().equals(displayName)) {
                return team;
            }
        }
        return null;
    }

    public Team getTeamByPlayer(Player player) {
        for (Team team : teams) {
            if (team.getTeamMembers().contains(player)) {
                return team;
            }
        }
        return null;
    }

    public Team getEmptiestTeam() {
        int teamSize = Integer.MAX_VALUE;
        Team emptiestTeam = null;
        for (Team team : teams) {
            if (team.getTeamMembers().size() < teamSize) {
                teamSize = team.getTeamMembers().size();
                emptiestTeam = team;
            }
        }
        return emptiestTeam;
    }

    public boolean hasTeam(Player player) {
        return getTeamByPlayer(player) != null;
    }

    public void leaveTeams(Player player) {
        for (Team team : teams) {
            team.getTeamMembers().remove(player);
        }
    }

    public void joinTeam(Player player, Team team) {
        team.getTeamMembers().add(player);
        team.getCompleteTeamMembers().add(player.getUniqueId());
    }

    public boolean teamsFilled() {
        int teamsAmount = 0;
        for (Team team : teams) {
            if (! team.getTeamMembers().isEmpty()) {
                teamsAmount++;
            }
        }
        return teamsAmount > 1;
    }

    public Team getMostPlayersTeam() {
        int teamSize = 0;
        Team mostTeam = null;
        for (Team team : teams) {
            if (team.getTeamMembers().size() > teamSize) {
                teamSize = team.getTeamMembers().size();
                mostTeam = team;
            }
        }
        return mostTeam;
    }

    public void balanceTeams() {
        int offSet = getMostPlayersTeam().getTeamMembers().size() - getEmptiestTeam().getTeamMembers().size();
        if (offSet >= plugin.getSettingsManager().getOffSet()) {
            PlayerUtils.parseThroughAllPlayer(player -> player.sendMessage(plugin.getSettingsManager().getPrefix() + "§7Durch unfaire Teamaufteilung wurden die Teams verteilt."));
            while (offSet >= plugin.getSettingsManager().getOffSet()) {
                Player victim = getMostPlayersTeam().getTeamMembers().get(getMostPlayersTeam().getTeamMembers().size() - 1);
                leaveTeams(victim);
                joinTeam(victim, getEmptiestTeam());
                offSet = getMostPlayersTeam().getTeamMembers().size() - getEmptiestTeam().getTeamMembers().size();
            }
        }
    }

    public List<Team> getTeams() {
        return teams;
    }

    public TeamColor getColorByDisplayName(String displayName) {
        for (TeamColor color : TeamColor.values()) {
            if (color.getDisplayName().equals(displayName)) {
                return color;
            }
        }
        return null;
    }

    public boolean isTeamFull(Team team) {
        return team.getTeamMembers().size() == plugin.getSettingsManager().getTeamSize();
    }

    public Team checkWinCondition() {
        if (this.teams.stream().filter(team -> ! team.getTeamMembers().isEmpty()).count() == 1) return teams.get(0);
        else return null;
    }

    public Location getLocationByTeam(Team team) {
        return plugin.getArenaManager().getCurrentArena().getLocation("team-" + team.getDisplayName());
    }

    public Team getNearbyTeamByLocation(final Location location) {
        double minDistance = 9999;
        Team nearbyTeam = null;
        for (final Team team : this.teams) {
            Location teamSpawn = getLocationByTeam(team);
            if (teamSpawn != null) {
                final double distance = teamSpawn.distance(location);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearbyTeam = team;
                }
            }
        }
        return nearbyTeam;
    }
}
