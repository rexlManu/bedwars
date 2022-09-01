/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.gamestates.states;

import de.rexlmanu.bedwars.entities.Stats;
import de.rexlmanu.bedwars.gamestates.GameState;
import de.rexlmanu.bedwars.managers.StatsManager;
import de.rexlmanu.bedwars.misc.npc.PacketReader;
import lombok.Setter;
import de.rexlmanu.bedwars.BedWars;
import de.rexlmanu.bedwars.callback.CooldownCallback;
import de.rexlmanu.bedwars.callback.EventListener;
import de.rexlmanu.bedwars.cooldown.Cooldown;
import de.rexlmanu.bedwars.entities.Team;
import de.rexlmanu.bedwars.managers.ScoreboardManager;
import de.rexlmanu.bedwars.utils.ItemBuilder;
import de.rexlmanu.bedwars.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 03:44                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

public final class EndingState extends GameState {

    @Setter
    private Team winnerTeam;

    private Cooldown cooldown;

    public EndingState(BedWars plugin) {
        super(plugin);
    }

    @Override
    public void start() {
        Bukkit.broadcastMessage(getPlugin().getSettingsManager().getPrefix() + "§7Team §9" + winnerTeam.getDisplayName() + "§7 hat gewonnen!");

        this.initListeners();
        this.initCooldowns();

        StatsManager statsManager = getPlugin().getStatsManager();
        winnerTeam.getCompleteTeamMembers().forEach(uuid -> statsManager.getStatsByPlayer(uuid).addWin());

        Location lobby = getPlugin().getSettingsManager().getLobby();
        PlayerUtils.parseThroughAllPlayer(player -> {
            player.sendTitle("§9Team "+winnerTeam.getDisplayName(), "§7hat gewonnen!");
            statsManager.getStatsByPlayer(player.getUniqueId()).addGame();
            PlayerUtils.clearPlayer(player);
            PlayerUtils.parseThroughAllPlayer(player::showPlayer);
            if(lobby != null) player.teleport(lobby);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
            player.getInventory().setItem(8, new ItemBuilder(Material.SLIME_BALL, 1).setDisplayname("§8» §9Verlassen §8● §7Rechtsklick").build());
        });

        PlayerUtils.parseThroughAllPlayer(all -> {
            all.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            ScoreboardManager manager = getPlugin().getScoreboardManager();
            manager.sendTeamTablist(all);
            manager.sendLobbyScoreboard(all, Bukkit.getOnlinePlayers().size());

            Stats stats = statsManager.getStatsByPlayer(all.getUniqueId());
            statsManager.spawnStatsHologram(all, stats);
            statsManager.saveStats(stats);
        });
    }

    @Override
    public void stop() {

    }

    private void initCooldowns() {
        this.cooldown = new Cooldown(15, getPlugin(), new CooldownCallback() {
            @Override
            public void onStop() {
                PlayerUtils.parseThroughAllPlayer(player -> {
                    player.setLevel(0);
                    player.setExp(0);
                    player.sendMessage(getPlugin().getSettingsManager().getPrefix() + "§7Der Server startet neu");
                    PlayerUtils.sendActionBar(player, "");
                    player.kickPlayer("");
                    Bukkit.shutdown();
                });
            }

            @Override
            public void onTick(int seconds) {
                PlayerUtils.parseThroughAllPlayer(player -> {
                    player.setLevel(seconds);
                    player.setExp((float) ((double) seconds / (double) cooldown.getStartTime()));
                    player.playSound(player.getLocation(), Sound.LAVA_POP, 1f, 1f);
                    PlayerUtils.sendActionBar(player, getPlugin().getSettingsManager().getPrefix() + "§7Der Server startet in §9" + seconds + "§7 " + (seconds == 1 ? "Sekunde." : "Sekunden neu."));
                });
            }
        });
        cooldown.start();
    }

    private void initListeners() {
        final String prefix = getPlugin().getSettingsManager().getPrefix();
        on(PlayerJoinEvent.class, (EventListener<PlayerJoinEvent>) event -> {
            event.getPlayer().kickPlayer("§7Das Spiel hat bereits geendet.");
            event.setJoinMessage(null);
        });
        on(PlayerQuitEvent.class, (EventListener<PlayerQuitEvent>) event -> {
            event.setQuitMessage(null);
            Player player = event.getPlayer();
            if (PacketReader.getPacketReaderMap().containsKey(player)) {
                PacketReader.getPacketReaderMap().get(player).uninject();
            }
        });
        on(PlayerArmorStandManipulateEvent.class, (EventListener<PlayerArmorStandManipulateEvent>) event -> event.setCancelled(true));
        on(BlockBreakEvent.class, (EventListener<BlockBreakEvent>) event -> event.setCancelled(true));
        on(BlockPlaceEvent.class, (EventListener<BlockPlaceEvent>) event -> event.setCancelled(true));
        on(PlayerInteractEvent.class, (EventListener<PlayerInteractEvent>) event -> {
            final Player player = event.getPlayer();
            event.setCancelled(true);
            if (event.getItem() == null) return;
            if (! event.getItem().getItemMeta().hasDisplayName()) return;
            if (event.getItem().getItemMeta().getDisplayName().equals("§8» §9Verlassen §8● §7Rechtsklick")) {
                player.kickPlayer("");
            }
        });
        on(EntityDamageEvent.class, (EventListener<EntityDamageEvent>) event -> event.setCancelled(true));
        on(FoodLevelChangeEvent.class, (EventListener<FoodLevelChangeEvent>) event -> event.setCancelled(true));
        on(EntityExplodeEvent.class, (EventListener<EntityExplodeEvent>) event -> event.setCancelled(true));
        on(EntitySpawnEvent.class, (EventListener<EntitySpawnEvent>) event -> event.setCancelled(true));
        on(WeatherChangeEvent.class, (EventListener<WeatherChangeEvent>) event -> event.setCancelled(true));
        on(LeavesDecayEvent.class, (EventListener<LeavesDecayEvent>) event -> event.setCancelled(true));
        /*on(AsyncPlayerChatEvent.class, (EventListener<AsyncPlayerChatEvent>) event -> {
            final Player player = event.getPlayer();
            final PermissionGroup group = CloudAPI.getInstance().getOnlinePlayer(player.getUniqueId()).getPermissionEntity().getHighestPermissionGroup(CloudAPI.getInstance().getPermissionPool());
            event.setFormat(group.getPrefix() + player.getName() + " §8» §7" + event.getMessage().replace("%", "%%"));
        });*/
        on(PlayerDropItemEvent.class, (EventListener<PlayerDropItemEvent>) event -> event.setCancelled(true));
        on(PlayerPickupItemEvent.class, (EventListener<PlayerPickupItemEvent>) event -> event.setCancelled(true));
        on(InventoryClickEvent.class, (EventListener<InventoryClickEvent>) event -> event.setCancelled(true));
        on(EntityDamageEvent.class, (EventListener<EntityDamageEvent>) event -> event.setCancelled(true));
        on(FoodLevelChangeEvent.class, (EventListener<FoodLevelChangeEvent>) event -> event.setCancelled(true));
        on(EntityDamageByEntityEvent.class, (EventListener<EntityDamageByEntityEvent>) event -> event.setCancelled(true));
        on(CreatureSpawnEvent.class, (EventListener<CreatureSpawnEvent>) event -> event.setCancelled(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM));
    }

}
