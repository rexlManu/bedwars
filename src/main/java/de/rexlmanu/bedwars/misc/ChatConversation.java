/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.misc;

import de.rexlmanu.bedwars.ManagerPlugin;
import de.rexlmanu.bedwars.callback.EventListener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 06:21                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

public class ChatConversation {

    private String keyWord;
    private List<Player> members;
    private String prefix;
    private boolean enabled;
    private String dontStartPrefix;

    public ChatConversation(ManagerPlugin managerPlugin) {
        this.members = new ArrayList<>();
        this.prefix = "";
        this.enabled = false;
        managerPlugin.getEventManager().registerEvent(AsyncPlayerChatEvent.class, (EventListener<AsyncPlayerChatEvent>) event -> {
            if (enabled) {
                if (keyWord == null || event.getMessage().toLowerCase().startsWith(keyWord)) {
                    if (dontStartPrefix == null || !event.getMessage().toLowerCase().startsWith(dontStartPrefix)) {
                        if (!event.isCancelled()) {
                            event.setCancelled(true);
                            members.forEach(player -> {
                                if (player != null) {
                                    if (keyWord != null) {
                                        player.sendMessage(prefix + event.getPlayer().getName() + " §8» §7" + event.getMessage().replaceFirst(keyWord + " ", "").replaceFirst(keyWord, ""));
                                    } else {
                                        player.sendMessage(prefix + event.getPlayer().getName() + " §8» §7" + event.getMessage());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public List<Player> getMembers() {
        return members;
    }

    public void setMembers(List<Player> members) {
        this.members = members;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDontStartPrefix() {
        return dontStartPrefix;
    }

    public void setDontStartPrefix(String dontStartPrefix) {
        this.dontStartPrefix = dontStartPrefix;
    }
}
