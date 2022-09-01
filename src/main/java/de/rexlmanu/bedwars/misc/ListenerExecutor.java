/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.misc;

import de.rexlmanu.bedwars.callback.EventListener;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 04:09                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

public final class ListenerExecutor implements EventExecutor {
    private final Class<? extends Event> cls;
    private final EventListener<Event> listener;
    private boolean disable;

    public ListenerExecutor(final Class<? extends Event> cls, final EventListener<Event> listener) {
        this.cls = cls;
        this.listener = listener;
    }

    public void execute(final Listener ll, final Event event) throws EventException {
        if (this.disable) {
            event.getHandlers().unregister(ll);
            return;
        }
        if (this.cls.equals(event.getClass())) {
            this.listener.on(event);
        }
    }

    public void setDisable(final boolean disable) {
        this.disable = disable;
    }

    public EventListener<Event> getListener() {
        return this.listener;
    }
}
