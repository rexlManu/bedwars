/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.gamestates;

import de.rexlmanu.bedwars.ManagerPlugin;
import de.rexlmanu.bedwars.callback.EventListener;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 03:40                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

public abstract class GameState {


    public static final int LOBBY_STATE = 0,
            INGAME_STATE = 1,
            ENDING_STATE = 2;

    private ManagerPlugin plugin;
    private final Map<Class<? extends Event>, ArrayList<EventListener>> listener;
    private List<String> commands;

    public GameState(ManagerPlugin plugin) {
        this.plugin = plugin;
        this.listener = new HashMap<>();
        this.commands = new ArrayList<>();
    }

    public void onStop() {
        stop();
        this.listener.keySet().stream().forEach(c -> this.listener.get(c).stream().forEach(l -> this.plugin.getEventManager().unregisterEvent(c, l)));
        commands.forEach(getPlugin().getEventManager()::removeCommand);
    }

    public void onStart() {
        start();
    }

    public abstract void start();

    public abstract void stop();

    protected void on(final Class<? extends Event> cls, final EventListener listener) {
        if (!this.listener.containsKey(cls)) {
            this.listener.put(cls, new ArrayList<>());
        }
        this.listener.get(cls).add(listener);
        this.plugin.getEventManager().registerEvent(cls, listener);
    }

    protected void on(final String cmd, final CommandExecutor executor) {
        commands.add(cmd);
        this.plugin.getEventManager().onCommand(cmd, executor);
    }

    public ManagerPlugin getPlugin() {
        return plugin;
    }
}
