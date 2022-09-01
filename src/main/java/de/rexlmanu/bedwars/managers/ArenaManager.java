/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.managers;

import de.rexlmanu.bedwars.entities.Arena;
import de.rexlmanu.bedwars.utils.PlayerUtils;
import lombok.Getter;
import lombok.Setter;
import de.rexlmanu.bedwars.ManagerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 01.09.2019 / 05:15                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public final class ArenaManager {

    private static final Random RANDOM = new Random();

    private ManagerPlugin managerPlugin;
    @Getter
    private Path mapDirectory;
    @Getter
    private List<Arena> arenas;

    @Setter
    @Getter
    private Arena currentArena;
    @Setter
    private boolean arenaForced;

    public ArenaManager(ManagerPlugin managerPlugin) {
        this.managerPlugin = managerPlugin;
        this.mapDirectory = Paths.get("./arenas");
        this.arenas = new LinkedList<>();
        this.arenaForced = false;
        if (! this.mapDirectory.toFile().exists()) this.mapDirectory.toFile().mkdirs();
    }

    public void pickMostVotedArena() {
        //force map check
        if (! this.arenaForced && somebodyVoted()) {
            Arena[] arrays = this.arenas.toArray(new Arena[0]);
            Arrays.sort(arrays);
            this.currentArena = arrays[arrays.length - 1];
        }

        String prefix = managerPlugin.getSettingsManager().getPrefix();
        Bukkit.broadcastMessage(prefix + "§7Die Map §9" + currentArena.getName() + "§7 wird gespielt.");
        PlayerUtils.parseThroughAllPlayer(player -> {
            player.sendTitle("§9" + currentArena.getName(), "§7" + currentArena.getBuilder());
        });
    }

    private boolean somebodyVoted() {
        return this.arenas.stream().anyMatch(arena -> ! arena.getVoters().isEmpty());
    }

    public void loadArenas() {
        for (File file : Objects.requireNonNull(this.mapDirectory.toFile().listFiles()))
            this.arenas.add(new Arena(this.managerPlugin, file));

        this.randomArena();
    }

    public Arena getArenaByName(String name) {
        return this.arenas.stream().filter(arena -> arena.getName().equals(name)).findFirst().orElse(null);
    }

    public void removeVotes(Player player) {
        this.arenas.forEach(arena -> arena.getVoters().remove(player));
    }

    public void randomArena() {
        if (! this.arenas.isEmpty())
            this.currentArena = this.arenas.get(RANDOM.nextInt(this.arenas.size()));
    }
}
