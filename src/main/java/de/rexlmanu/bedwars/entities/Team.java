/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.entities;

import com.google.common.collect.Lists;
import lombok.Data;
import de.rexlmanu.bedwars.ManagerPlugin;
import de.rexlmanu.bedwars.misc.ChatConversation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 06:19                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

@Data
public class Team {

    private String displayName;
    private List<Player> teamMembers;
    private List<UUID> completeTeamMembers;
    private ChatConversation chatConversation;
    private ManagerPlugin plugin;
    private boolean bed;
    private Inventory teamChest;

    public Team(String displayName, ManagerPlugin plugin) {
        this.displayName = displayName;
        this.plugin = plugin;
        this.bed = true;
        this.teamMembers = new ArrayList<>();
        this.completeTeamMembers = Lists.newLinkedList();
        this.chatConversation = new ChatConversation(plugin);
        this.chatConversation.setMembers(teamMembers);
        this.chatConversation.setPrefix("§9Team §8» §7");
        this.teamChest = Bukkit.createInventory(null, 4 * 9, "§8» §9Teamchest");
    }
}
