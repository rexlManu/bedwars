/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.cooldown;

import de.rexlmanu.bedwars.ManagerPlugin;
import de.rexlmanu.bedwars.callback.CooldownCallback;
import org.bukkit.scheduler.BukkitTask;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 05:06                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

public final class Cooldown {

    private int startTime;
    private int seconds;
    private ManagerPlugin managerPlugin;
    private CooldownCallback callback;
    private BukkitTask task;

    public Cooldown(int startTime, ManagerPlugin managerPlugin, CooldownCallback callback) {
        this.startTime = startTime;
        this.managerPlugin = managerPlugin;
        this.callback = callback;
        this.seconds = startTime;
    }

    public void start() {
        task = managerPlugin.schedule(0, 20, () -> {
            callback.onTick(seconds);
            seconds--;

            if (seconds == 0) {
                stop();
            }
        });
    }

    public void stop(boolean skipStopAction) {
        if (isRunning()) {
            task.cancel();
            task = null;
            if (!skipStopAction)
                callback.onStop();
        } else throw new IllegalStateException("The cooldown is already null.");
    }

    public void resetCountdown() {
        setSeconds(startTime + 1);
    }

    public void stop() {
        stop(false);
    }

    public boolean isRunning() {
        return task != null;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
}
