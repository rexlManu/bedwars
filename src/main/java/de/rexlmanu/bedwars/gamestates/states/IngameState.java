/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.gamestates.states;

import de.rexlmanu.bedwars.gamestates.GameState;
import de.rexlmanu.bedwars.misc.npc.NPC;
import de.rexlmanu.bedwars.misc.npc.PacketReader;
import lombok.Getter;
import de.rexlmanu.bedwars.BedWars;
import de.rexlmanu.bedwars.callback.EventListener;
import de.rexlmanu.bedwars.entities.Arena;
import de.rexlmanu.bedwars.entities.Stats;
import de.rexlmanu.bedwars.entities.Team;
import de.rexlmanu.bedwars.managers.ScoreboardManager;
import de.rexlmanu.bedwars.managers.SettingsManager;
import de.rexlmanu.bedwars.managers.SpectatorManager;
import de.rexlmanu.bedwars.managers.TeamManager;
import de.rexlmanu.bedwars.misc.ChatConversation;
import de.rexlmanu.bedwars.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

public final class IngameState extends GameState {

    private List<Player> playingPlayers;
    private List<Player> spectators;
    private ChatConversation globalChat;
    @Getter
    private static SpectatorManager spectatorManager;
    private List<Block> breakedBlocks;


    public IngameState(BedWars plugin) {
        super(plugin);

        this.playingPlayers = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.spectatorManager = new SpectatorManager(spectators, playingPlayers);
        this.breakedBlocks = new LinkedList<>();
    }

    @Override
    public void start() {
        initListeners();
        initTeams();

        getPlugin().getStatsManager().removeHolograms();

        playingPlayers.addAll(Bukkit.getOnlinePlayers());
        final TeamManager teamManager = getPlugin().getTeamManager();
        PlayerUtils.parseThroughAllPlayer(player -> {
            if (! teamManager.hasTeam(player)) {
                teamManager.joinTeam(player, teamManager.getEmptiestTeam());
            }
        });

        SettingsManager settingsManager = getPlugin().getSettingsManager();
        Bukkit.broadcastMessage(settingsManager.getPrefix() + "§9Spieleinstellungen: §7Gold §8» §9"
                + (settingsManager.isGold() ? "✔" : "✖") + " §8︳ §7Cobweb §8» §9"
                + (settingsManager.isCobweb() ? "✔" : "✖"));

        teamManager.balanceTeams();
        teamManager.getTeams().forEach(team -> {
            team.getChatConversation().setEnabled(true);
            team.getChatConversation().setDontStartPrefix("@all");
        });
        this.globalChat = new ChatConversation(getPlugin());
        this.globalChat.setEnabled(true);
        this.globalChat.setKeyWord("@all");
        this.globalChat.setPrefix("§9Global §8» §7");
        this.globalChat.setMembers(playingPlayers);

        Arena currentArena = getPlugin().getArenaManager().getCurrentArena();
        PlayerUtils.parseThroughAllPlayer(player -> {
            final Team team = teamManager.getTeamByPlayer(player);
            player.sendMessage(getPlugin().getSettingsManager().getPrefix() + "§7Das Spiel beginnt nun.");
            player.sendMessage(settingsManager.getPrefix() + "§7Du bist im Team §9" + team.getDisplayName() + "§7.");
            this.getPlugin().getArenaManager().getCurrentArena().teleportPlayer(player, "team-" + team.getDisplayName());
            PlayerUtils.clearPlayer(player);
            Location npcLocation = currentArena.getLocation("villager-" + team.getDisplayName());
            if (npcLocation != null) {
                getPlugin().getNpcManager().spawnNPC(player, npcLocation);
            }
        });

        PlayerUtils.parseThroughAllPlayer(all -> {
            ScoreboardManager manager = getPlugin().getScoreboardManager();
            manager.sendTeamTablist(all);
            manager.sendLobbyScoreboard(all, playingPlayers.size());
        });

        this.getPlugin().getArenaManager().getCurrentArena().getSpawnerManager().enableSpawners();

    }

    private void initTeams() {
   /*     for (int i = 0; i <= getPlugin().getSettingsManager().getTeams(); i++) {
            final TeamColor teamColor = TeamColor.values()[i];
            getPlugin().getTeamManager().registerTeam(teamColor.getDisplayName());
        }*/
    }

    private void initListeners() {
        final String prefix = getPlugin().getSettingsManager().getPrefix();
        on(PlayerJoinEvent.class, (EventListener<PlayerJoinEvent>) event -> {
            event.setJoinMessage(null);
            final Player player = event.getPlayer();
            spectators.add(player);
            //getPlugin().getLocationManager().teleportPlayer(player, "spectator");
            this.getPlugin().getArenaManager().getCurrentArena().teleportPlayer(player, "spectator");
            spectatorManager.setAsSpectator(player);
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

            getPlugin().getScoreboardManager().sendTeamTablist(player);
            getPlugin().getScoreboardManager().sendLobbyScoreboard(player, playingPlayers.size());
        });
        on(PlayerQuitEvent.class, (EventListener<PlayerQuitEvent>) event -> {
            event.setQuitMessage(null);
            final Player player = event.getPlayer();
            if (PacketReader.getPacketReaderMap().containsKey(player)) {
                PacketReader.getPacketReaderMap().get(player).uninject();
            }

            Team teamByPlayer = getPlugin().getTeamManager().getTeamByPlayer(player);
            if (teamByPlayer != null) teamByPlayer.getTeamMembers().remove(player);
            // Ragequit check
            if (! spectatorManager.isSpectator(player)) {
                Team winTeam = getPlugin().getTeamManager().checkWinCondition();
                Stats statsByPlayer = getPlugin().getStatsManager().getStatsByPlayer(player.getUniqueId());
                statsByPlayer.addDeath();
                if (winTeam != null) {
                    ((EndingState) this.getPlugin().getGameManager().getGameStates().get(GameState.ENDING_STATE)).setWinnerTeam(winTeam);
                    this.getPlugin().getGameManager().setCurrentState(GameState.ENDING_STATE);
                } else {
                    statsByPlayer.addGame();
                    getPlugin().getStatsManager().saveStats(statsByPlayer);
                }
                return;
            }
            spectators.remove(player);

        });

        on(PlayerBedEnterEvent.class, (EventListener<PlayerBedEnterEvent>) event -> event.setCancelled(true));
        on(PlayerArmorStandManipulateEvent.class, (EventListener<PlayerArmorStandManipulateEvent>) event -> event.setCancelled(true));
        on(FoodLevelChangeEvent.class, (EventListener<FoodLevelChangeEvent>) event -> event.setCancelled(true));
        on(EntitySpawnEvent.class, (EventListener<EntitySpawnEvent>) event -> event.setCancelled(true));
        on(WeatherChangeEvent.class, (EventListener<WeatherChangeEvent>) event -> event.setCancelled(true));
        on(LeavesDecayEvent.class, (EventListener<LeavesDecayEvent>) event -> event.setCancelled(true));
        on(BlockPlaceEvent.class, (EventListener<BlockPlaceEvent>) event -> {
            Team team = getPlugin().getTeamManager().getNearbyTeamByLocation(event.getBlock().getLocation());
            if (event.getBlock().getLocation().distance(getPlugin().getTeamManager().getLocationByTeam(team)) < 1.7D) {
                event.setCancelled(true);
                event.setBuild(true);
                event.getPlayer().sendMessage(getPlugin().getSettingsManager().getPrefix() + "§7Bitte setze keine Blöcke am Spawn.");
                return;
            }
            if (! getPlugin().getArenaManager().getCurrentArena().isInArena(event.getBlock().getLocation())) {
                event.setCancelled(true);
                event.setBuild(true);
                event.getPlayer().sendMessage(getPlugin().getSettingsManager().getPrefix() + "§7Du bist außerhalb des Spielfeldes.");
                return;
            }
            this.breakedBlocks.add(event.getBlock());
        });
        on(CreatureSpawnEvent.class, (EventListener<CreatureSpawnEvent>) event -> event.setCancelled(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM));
        on(PlayerInteractAtEntityEvent.class, (EventListener<PlayerInteractAtEntityEvent>) event -> {
            if (event.getRightClicked() == null) return;
            if (! event.getRightClicked().isCustomNameVisible()) return;
            if (! event.getRightClicked().getCustomName().equals("§8» §9§lShop")) return;
            event.setCancelled(true);
            /*Player player = event.getPlayer();
            player.playSound(player.getLocation(), Sound.CHEST_OPEN, 3f, 3f);
            getPlugin().getShopManager().openShop(player, null, null);*/
        });
        on(PlayerDeathEvent.class, (EventListener<PlayerDeathEvent>) event -> {
            Player player = event.getEntity();
            Player killer = player.getKiller();
            if (killer == null) Bukkit.broadcastMessage(prefix + "§7" + player.getName() + " ist gestorben.");
            else
                Bukkit.broadcastMessage(prefix + "§9" + player.getName() + "§7 wurde von §9" + killer.getName() + "§7 getötet.");
            player.spigot().respawn();
            event.setDeathMessage(null);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            event.setKeepInventory(true);
            Team team = getPlugin().getTeamManager()
                    .getTeamByPlayer(player);
            if (! team.isBed()) {
                team.getChatConversation().getMembers().remove(player);
                team.getTeamMembers().remove(player);
                spectatorManager.setAsSpectator(player);
                getPlugin().getStatsManager().getStatsByPlayer(player.getUniqueId()).addDeath();
                if (killer != null)
                    getPlugin().getStatsManager().getStatsByPlayer(killer.getUniqueId()).addKill();
            } else {
                player.setVelocity(new Vector(0, 0, 0));
                player.setFireTicks(0);
                //this.getPlugin().getLocationManager().teleportPlayer(player, "team-" + team.getDisplayName().toLowerCase());
                this.getPlugin().getArenaManager().getCurrentArena().teleportPlayer(player, "team-" + team.getDisplayName());

                for (final NPC npc : getPlugin().getNpcManager().getPlayerNPCMap().get(player)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (final NPC npc : getPlugin().getNpcManager().getPlayerNPCMap().get(player)) {
                                npc.destroy();
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        npc.spawn();
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                npc.removeFromTablist();
                                            }
                                        }.runTaskLater(getPlugin(), 10);
                                    }
                                }.runTaskLater(getPlugin(), 10);
                            }
                        }
                    }.runTaskLater(getPlugin(), 10);
                }

            }

            PlayerUtils.parseThroughAllPlayer(all -> {
                ScoreboardManager manager = getPlugin().getScoreboardManager();
                manager.sendLobbyScoreboard(all, playingPlayers.size());
            });

            Team winTeam = getPlugin().getTeamManager().checkWinCondition();
            if (winTeam != null) {
                ((EndingState) this.getPlugin().getGameManager().getGameStates().get(GameState.ENDING_STATE)).setWinnerTeam(winTeam);
                this.getPlugin().getGameManager().setCurrentState(GameState.ENDING_STATE);
            }
        });
        on(PrepareItemCraftEvent.class, (EventListener<PrepareItemCraftEvent>) event -> event.getInventory().setResult(null));
        on(ItemSpawnEvent.class, (EventListener<ItemSpawnEvent>) event -> {
            if (event.getEntity().getItemStack().getType().equals(Material.BED)) event.setCancelled(true);
        });
        on(BlockBreakEvent.class, (EventListener<BlockBreakEvent>) event -> {
            if (! this.breakedBlocks.contains(event.getBlock())) event.setCancelled(true);
            if (event.getBlock().getType().equals(Material.BED_BLOCK)) {
                Player player = event.getPlayer();
                Team team = getPlugin().getTeamManager().getTeamByPlayer(player);
                Team nearbyTeam = getPlugin().getTeamManager().getNearbyTeamByLocation(event.getBlock().getLocation());
                if (nearbyTeam == null) {
                    player.sendMessage("Team is null... impossible");
                    event.setCancelled(true);
                    return;
                }
                if (team.equals(nearbyTeam)) {
                    player.sendMessage(prefix + "§7Bitte baue nicht dein §ceigenes §7Bett ab.");
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(false);
                event.getBlock().getDrops().clear();
                Bukkit.broadcastMessage(prefix + "§7Das Bett von Team §9" + nearbyTeam.getDisplayName() + "§7 wurde von §9" + player.getName() + "§7 zerstört.");
                nearbyTeam.getTeamMembers().forEach(teamMember -> teamMember.sendTitle("§r", "§7Dein Bett wurde von §9" + player.getName() + "§7 zerstört."));
                nearbyTeam.setBed(false);
                getPlugin().getStatsManager().getStatsByPlayer(player.getUniqueId()).addBed();
            } else {
                breakedBlocks.remove(event.getBlock());
            }
        });
        on(EntityDamageEvent.class, (EventListener<EntityDamageEvent>) event -> {
            if (event.getEntity().getType().equals(EntityType.ARMOR_STAND))
                event.setCancelled(event.getEntity().isCustomNameVisible() && event.getEntity().getCustomName().equals("§8» §9§lShop"));
            if (! (event.getEntity() instanceof Player)) return;
            Player player = (Player) event.getEntity();
            if (spectatorManager.isSpectator(player)) {
                event.setCancelled(true);
                return;
            }
            if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                ((Player) event.getEntity()).setHealth(0);
            }

        });
        on(EntityDamageByEntityEvent.class, (EventListener<EntityDamageByEntityEvent>) event -> {
            if (event.getEntity().isCustomNameVisible() && event.getEntity().getCustomName().equals("§8» §9§lShop"))
                event.setCancelled(true);
            if (! (event.getDamager() instanceof Player) || ! (event.getEntity() instanceof Player)) return;
            event.setCancelled(getPlugin().getTeamManager().getTeamByPlayer((Player) event.getEntity())
                    .getTeamMembers().contains((event.getDamager())) || spectatorManager.isSpectator((Player) event.getDamager()));
        });
        on(PlayerInteractAtEntityEvent.class, (EventListener<PlayerInteractAtEntityEvent>) event -> event.setCancelled(
                event.getRightClicked() != null && event.getRightClicked().isCustomNameVisible()
                        && event.getRightClicked().getCustomName().equals("§8» §9§lShop")));
        on(CreatureSpawnEvent.class, (EventListener<CreatureSpawnEvent>) event -> event.setCancelled(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM));
    }

    @Override
    public void stop() {
        this.getPlugin().getArenaManager().getCurrentArena().getSpawnerManager().disableSpawners();
        this.globalChat.setEnabled(false);
        this.getPlugin().getTeamManager().getTeams().forEach(team -> team.getChatConversation().setEnabled(false));
        this.spectatorManager.visibleSpectators();
    }
}
