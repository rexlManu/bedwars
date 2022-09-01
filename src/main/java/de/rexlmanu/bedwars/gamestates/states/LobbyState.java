/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.gamestates.states;

import com.google.common.base.Enums;
import de.rexlmanu.bedwars.BedWars;
import de.rexlmanu.bedwars.callback.CooldownCallback;
import de.rexlmanu.bedwars.callback.EventListener;
import de.rexlmanu.bedwars.cooldown.Cooldown;
import de.rexlmanu.bedwars.entities.Arena;
import de.rexlmanu.bedwars.entities.Team;
import de.rexlmanu.bedwars.gamestates.GameState;
import de.rexlmanu.bedwars.managers.*;
import de.rexlmanu.bedwars.misc.TeamColor;
import de.rexlmanu.bedwars.misc.npc.PacketReader;
import de.rexlmanu.bedwars.utils.InventoryFill;
import de.rexlmanu.bedwars.utils.ItemBuilder;
import de.rexlmanu.bedwars.utils.PlayerUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 22.07.2018 / 03:43                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

public final class LobbyState extends GameState {

    private List<Player> lobbyPlayers;
    private Cooldown cooldown;
    private List<Player> withGold, withoutGold, withCobweb, withoutCobweb;
    private boolean editMode;

    public LobbyState(BedWars plugin) {
        super(plugin);
        this.lobbyPlayers = new ArrayList<>();
        this.withGold = new ArrayList<>();
        this.withoutGold = new ArrayList<>();
        this.withCobweb = new ArrayList<>();
        this.withoutCobweb = new ArrayList<>();
        this.editMode = false;
    }

    @Override
    public void start() {

        if (getPlugin().getArenaManager().getArenas().isEmpty()) {
            this.editMode = true;
        }

        initListeners();
        initCooldowns();
        initCommands();
    }

    private void initCommands() {
        String prefix = getPlugin().getSettingsManager().getPrefix();
        on("start", (sender, command, label, args) -> {
            if (!sender.hasPermission("bedwars.start")) {
                sender.sendMessage(prefix + "§7Dazu hast du keine Berechtigung.");
                return true;
            }
            if (lobbyPlayers.size() < getPlugin().getSettingsManager().getMinPlayers()) {
                sender.sendMessage(prefix + "§7In der Runde sind nicht §9genug §7Spieler.");
                return true;
            }
            if (cooldown.getSeconds() < 11) {
                sender.sendMessage(prefix + "§7Das Spiel startet bereits gleich.");
            } else {
                cooldown.setSeconds(10);
                sender.sendMessage(prefix + "§7Das Spiel wurde erfolgreich beschleunigt.");
                PlayerUtils.parseThroughAllPlayer(player -> player.getInventory().setItem(4, null));
            }

            return true;
        });
        on("bw", (sender, command, label, args) -> {
            if (!sender.hasPermission("bedwars.setup")) {
                sender.sendMessage(prefix + "§7Dazu hast du keine Berechtigung.");
                return true;
            }
            Player player = (Player) sender;
            String firstArgument = null;
            try {
                firstArgument = args[0].toLowerCase();
            } catch (Exception ignored) {

            }
            switch (args.length) {
                case 0:
                    sender.sendMessage("§7/bw wartelobby");
                    sender.sendMessage("§7/bw lobby");
                    sender.sendMessage("§7/bw hologram");
                    sender.sendMessage("§7/bw edit");
                    sender.sendMessage("§7/bw team <Arena> <TeamName>");
                    sender.sendMessage("§7/bw spawner <Arena> <Type>");
                    sender.sendMessage("§7/bw pos <Arena> <1/2>");
                    sender.sendMessage("§7/bw spectator <Arena>");
                    sender.sendMessage("§7/bw villager <Arena> <TeamName>");
                    sender.sendMessage("§7/bw create <Name> <Builder>");
                    sender.sendMessage("§7/bw tp <Name>");
                    sender.sendMessage("§7/bw ranking <Rank>");
                    break;
                case 1:
                    switch (firstArgument) {
                        case "lobby":
                        case "hologram":
                            getPlugin().getSettingsManager().setLocation(args[0].toLowerCase(), player.getLocation());
                            sender.sendMessage(prefix + "§7Du hast erfolgreich den Punkt §9" + firstArgument + "§7 gesetzt.");
                            break;
                        case "edit":
                            editMode = !editMode;
                            player.sendMessage(prefix + "§7Der §9Edit-Mode wurde " + (editMode ? "§aaktiviert" : "§cdeaktiviert") + "§7.");
                            break;
                        case "wartelobby":
                            World world = Bukkit.getWorld("Wartelobby");
                            if (world == null) {
                                player.sendMessage(prefix + "§7Es wurde noch keine Wartelobby hinzugefügt.");
                                return true;
                            }
                            player.teleport(world.getSpawnLocation());
                            break;
                    }
                    break;
                case 2:
                    if (firstArgument.equals("spectator")) {
                        Arena arena = getPlugin().getArenaManager().getArenaByName(args[1]);
                        if (arena == null) {
                            player.sendMessage(prefix + "§7Die Arena §9" + args[1] + " §7konnte nicht gefunden werden.");
                            return true;
                        }
                        arena.setLocation("spectator", player.getLocation());
                        player.sendMessage(prefix + "§7Du hast erfolgreich den §9Spectator-Spawn§7 gesetzt.");
                    } else if (firstArgument.equals("ranking")) {
                        try {
                            int rank = Integer.parseInt(args[1]);
                            if (rank < 1 || rank > 10) {
                                player.sendMessage(prefix + "§7Der Rank darf nur zwischen 1 und 10 sein.");
                                return true;
                            }
                            Block targetBlock = player.getTargetBlock((HashSet<Byte>) null, 10);
                            if (!targetBlock.getType().equals(Material.SKULL)) {
                                player.sendMessage(prefix + "§7Das Ranking muss mit einem §9Kopf ausgestattet sein.");
                                return true;
                            }
                            getPlugin().getSettingsManager().setLocation("ranking-" + (rank - 1), targetBlock.getLocation());
                            player.sendMessage(prefix + "§7Du hast erfolgreich das Ranking für Platz §9" + rank + "§7 gesetzt.");
                        } catch (NumberFormatException $e) {
                            player.sendMessage(prefix + "§7Dies ist keine Zahl.");
                            return true;
                        }
                    } else if (firstArgument.equals("tp")) {
                        World world = Bukkit.getWorld(args[1]);
                        if (world == null) {
                            player.sendMessage(prefix + "§7Die Welt konnte nicht gefunden werden.");
                            return true;
                        }
                        player.teleport(world.getSpawnLocation());
                        player.sendMessage(prefix + "§7Du wurdest teleportiert.");
                    }
                    break;
                case 3:
                    if (firstArgument.equals("create")) {
                        String name = args[1];
                        String builder = args[2];
                        if (getPlugin().getArenaManager().getArenaByName(name) != null) {
                            player.sendMessage(prefix + "§7Es gibt bereits eine Map mit dem Namen §9" + name + "§7.");
                            return true;
                        }
                        getPlugin().getArenaManager().getArenas().add(Arena.createNewMap(this.getPlugin(), name, builder));
                        player.sendMessage(prefix + "§7Du hast erfolgreich die Map §9" + name + "§7 erstellt.");
                        if (!new File(name).exists()) {
                            player.sendMessage(prefix + "§7Die Welt konnte nicht importiert werden weil der Ordner nicht existiert.");
                            return true;
                        }
                        Bukkit.createWorld(WorldCreator.name(name));
                        player.sendMessage(prefix + "§7Die Welt wurde erfolgreich importiert.");
                        player.teleport(Bukkit.getWorld(name).getSpawnLocation());
                        return true;
                    }
                    if (!firstArgument.equals("team") && !firstArgument.equals("spawner") && !firstArgument.equals("pos") && !firstArgument.equals("villager"))
                        return true;

                    Arena arena = getPlugin().getArenaManager().getArenaByName(args[1]);
                    if (arena == null) {
                        player.sendMessage(prefix + "§7Die Arena §9" + args[1] + " §7konnte nicht gefunden werden.");
                        return true;
                    }

                    switch (firstArgument) {
                        case "team":
                            Team team = getPlugin().getTeamManager().getTeamByDisplayName(args[2]);
                            if (team == null) {
                                player.sendMessage(prefix + "§7Das Team §9" + args[2] + " §7konnte nicht gefunden werden.");
                                return true;
                            }
                            arena.setLocation("team-" + team.getDisplayName(), player.getLocation());
                            player.sendMessage(prefix + "§7Du hast erfolgreich den Punkt für Team §9" + team.getDisplayName() + "§7 gesetzt.");
                            break;
                        case "villager":
                            Team teamFound = getPlugin().getTeamManager().getTeamByDisplayName(args[2]);
                            if (teamFound == null) {
                                player.sendMessage(prefix + "§7Das Team §9" + args[2] + " §7konnte nicht gefunden werden.");
                                return true;
                            }
                            arena.setLocation("villager-" + teamFound.getDisplayName(), player.getLocation());
                            player.sendMessage(prefix + "§7Du hast erfolgreich den Villager für Team §9" + teamFound.getDisplayName() + "§7 gesetzt.");
                            break;
                        case "spawner":
                            if (!Enums.getIfPresent(SpawnerManager.SpawnerType.class, args[2].toUpperCase()).isPresent()) {
                                player.sendMessage(prefix + "§7Der SpawnerType §9" + args[2] + " §7konnte nicht gefunden werden.");
                                return true;
                            }
                            SpawnerManager.SpawnerType spawnerType = SpawnerManager.SpawnerType.valueOf(args[2].toUpperCase());
                            arena.addSpawner(spawnerType, player.getLocation());
                            player.sendMessage(prefix + "§7Du hast erfolgreich den §9" + spawnerType.name() + "-Spawner§7 gesetzt.");
                            break;
                        case "pos":
                            if (!args[2].equals("1") && !args[2].equals("2")) {
                                player.sendMessage(prefix + "§7Bitte nutze nur 1 oder 2.");
                                return true;
                            }
                            arena.setLocation("pos" + args[2], player.getLocation());
                            player.sendMessage(prefix + "§7Du hast erfolgreich §9Position" + args[2] + "§7 gesetzt.");
                            break;
                    }


                    break;
            }
            return true;
        });

        on("force", (commandSender, command, s, strings) -> {
            if (!commandSender.hasPermission("venium.forcemap")) return false;
            if (cooldown.getSeconds() < 11) {
                commandSender.sendMessage(getPlugin().getSettingsManager().getPrefix() + "§7Die Map wurde bereits entschieden.");
                return true;
            }
            Player player = (Player) commandSender;
            openArenaVoter((Player) commandSender, true, null);
            player.playSound(player.getLocation(), Sound.CHEST_OPEN, 10f, 10f);
            return true;
        });
    }

    private void initCooldowns() {
        this.cooldown = new Cooldown(60, getPlugin(), new CooldownCallback() {
            @Override
            public void onStop() {
                getPlugin().getGameManager().setCurrentState(GameState.INGAME_STATE);
                PlayerUtils.parseThroughAllPlayer(player -> {
                    player.setLevel(0);
                    player.setExp(0);
                    PlayerUtils.sendActionBar(player, "");
                });
            }

            @Override
            public void onTick(int seconds) {
                if (editMode) {
                    PlayerUtils.parseThroughAllPlayer(player -> PlayerUtils.sendActionBar(player, getPlugin().getSettingsManager().getPrefix() + "§7Zurzeit ist der §9Edit-Mode §aaktiviert§7."));
                    cooldown.resetCountdown();
                    return;
                }
                boolean rested = lobbyPlayers.size() < getPlugin().getSettingsManager().getMinPlayers();
                if (rested) {
                    PlayerUtils.parseThroughAllPlayer(player -> {
                        if (player.hasPermission("bedwars.start")) {
                            if (player.getInventory().getItem(4) == null)
                                player.getInventory().setItem(4, new ItemBuilder(Material.DIAMOND, 1)
                                        .setDisplayname("§8» §9Spielstart §8● §7Rechtsklick").build());
                        }
                    });
                    cooldown.resetCountdown();
                    if (seconds < 11) getPlugin().getArenaManager().randomArena();
                }
                if (seconds == 10 && !rested) {
                    getPlugin().getArenaManager().pickMostVotedArena();
                }
                PlayerUtils.parseThroughAllPlayer(player -> {
                    if (seconds == 10 && !rested)
                        getPlugin().getScoreboardManager().sendLobbyScoreboard(player, Bukkit.getOnlinePlayers().size());
                    player.setLevel(seconds);
                    player.setExp((float) ((double) seconds / (double) cooldown.getStartTime()));
                    if (seconds % 10 == 0 || seconds < 11) {
                        if (!rested) {
                            if (seconds == 10) {
                                player.getInventory().setItem(5, null);
                                player.getInventory().setItem(4, null);
                                player.getInventory().setItem(3, null);
                                if (player.getOpenInventory() != null) {
                                    player.closeInventory();
                                }
                            }
                            player.playSound(player.getLocation(), Sound.LAVA_POP, 1f, 1f);
                        }
                        PlayerUtils.sendActionBar(player, getPlugin().getSettingsManager().getPrefix() + "§7Das Spiel startet in §9" + seconds + "§7 " + (seconds == 1 ? "Sekunde." : "Sekunden."));
                    }
                });
            }
        });
        cooldown.start();
    }

    private void initListeners() {
        final String prefix = getPlugin().getSettingsManager().getPrefix();
        on(PlayerJoinEvent.class, (EventListener<PlayerJoinEvent>) event -> {
            final Player player = event.getPlayer();
            if (lobbyPlayers.size() >= getPlugin().getSettingsManager().getMaxPlayers()) {
                player.kickPlayer("§7Der Server ist bereits voll. §8[§7" + lobbyPlayers.size() + "§8/§7" + getPlugin().getSettingsManager().getMaxPlayers() + "§8]");
                return;
            }
            if (editMode && !player.hasPermission("bw.admin")) {
                player.kickPlayer("§7Der Server ist bereits voll. §8[§7" + getPlugin().getSettingsManager().getMaxPlayers() + "§8/§7" + getPlugin().getSettingsManager().getMaxPlayers() + "§8]");
            }
            lobbyPlayers.add(player);
            event.setJoinMessage(prefix + "§7Der Spieler §9" + player.getName() + "§7 hat den Server betreten. §8[§7" + lobbyPlayers.size() + "§8/§7" + getPlugin().getSettingsManager().getMaxPlayers() + "§8]");
            Location lobby = getPlugin().getSettingsManager().getLobby();
            if (lobby != null) player.teleport(lobby);
            PlayerInventory inventory = player.getInventory();
            PlayerUtils.clearPlayer(player);
            player.sendTitle("§9BedWars", "§l§9rexlManu.de");
            inventory.setItem(8, new ItemBuilder(Material.SLIME_BALL, 1).setDisplayname("§8» §9Verlassen §8● §7Rechtsklick").build());
            inventory.setItem(0, new ItemBuilder(Material.BED, 1).setDisplayname("§8» §9Teamauswahl §8● §7Rechtsklick").build());
            if (player.hasPermission("bedwars.start")) {
                inventory.setItem(4, new ItemBuilder(Material.DIAMOND, 1).setDisplayname("§8» §9Spielstart §8● §7Rechtsklick").build());
            }
            inventory.setItem(5, new ItemBuilder(Material.HOPPER, 1).setDisplayname("§8» §9Mapauswahl §8● §7Rechtsklick").build());
            inventory.setItem(3, new ItemBuilder(Material.REDSTONE_COMPARATOR, 1).setDisplayname("§8» §9Abstimmung §8● §7Rechtsklick").build());

            final PacketReader value = new PacketReader(player, getPlugin());
            value.inject();
            PacketReader.getPacketReaderMap().put(player, value);

            StatsManager statsManager = getPlugin().getStatsManager();
            statsManager.loadStats(player.getUniqueId()).whenComplete((stats, throwable) -> {
                if (throwable != null || stats == null) {
                    player.sendMessage(prefix + "§7Es ist ein Fehler beim Laden deiner Stats aufgetreten.");
                    return;
                }
                statsManager.getStatsList().add(stats);
                statsManager.spawnStatsHologram(player, stats);
            });

            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

            PlayerUtils.parseThroughAllPlayer(all -> {
                ScoreboardManager manager = getPlugin().getScoreboardManager();
                manager.sendRanksTablist(all);
                manager.sendLobbyScoreboard(all, Bukkit.getOnlinePlayers().size());
            });
        });
        on(PlayerQuitEvent.class, (EventListener<PlayerQuitEvent>) event -> {
            final Player player = event.getPlayer();
            if (PacketReader.getPacketReaderMap().containsKey(player)) {
                PacketReader.getPacketReaderMap().get(player).uninject();
            }
            lobbyPlayers.remove(player);
            withGold.remove(player);
            withoutGold.remove(player);
            withCobweb.remove(player);
            withoutCobweb.remove(player);
            getPlugin().getArenaManager().removeVotes(player);
            event.setQuitMessage(prefix + "§7Der Spieler §9" + player.getName() + "§7 hat den Server verlassen. §8[§7" + lobbyPlayers.size() + "§8/§7" + getPlugin().getSettingsManager().getMaxPlayers() + "§8]");
            getPlugin().getTeamManager().leaveTeams(player);

            getPlugin().getStatsManager().getStatsList().remove(getPlugin().getStatsManager().getStatsByPlayer(player.getUniqueId()));

            PlayerUtils.parseThroughAllPlayer(all -> {
                ScoreboardManager manager = getPlugin().getScoreboardManager();
                manager.sendRanksTablist(all);
                manager.sendLobbyScoreboard(all, Bukkit.getOnlinePlayers().size());
            });
        });
        on(BlockBreakEvent.class, (EventListener<BlockBreakEvent>) event -> event.setCancelled(!editMode));
        on(BlockPlaceEvent.class, (EventListener<BlockPlaceEvent>) event -> event.setCancelled(!editMode));
        on(EntityDamageEvent.class, (EventListener<EntityDamageEvent>) event -> event.setCancelled(!editMode));
        /*on(AsyncPlayerChatEvent.class, (EventListener<AsyncPlayerChatEvent>) event -> {
            final Player player = event.getPlayer();
            final PermissionGroup group = CloudAPI.getInstance().getOnlinePlayer(player.getUniqueId()).getPermissionEntity().getHighestPermissionGroup(CloudAPI.getInstance().getPermissionPool());
            event.setFormat(group.getPrefix() + player.getName() + " §8» §7" + event.getMessage().replace("%", "%%"));
        });*/
        on(FoodLevelChangeEvent.class, (EventListener<FoodLevelChangeEvent>) event -> event.setCancelled(true));
        on(EntityExplodeEvent.class, (EventListener<EntityExplodeEvent>) event -> event.setCancelled(true));
        on(EntitySpawnEvent.class, (EventListener<EntitySpawnEvent>) event -> event.setCancelled(!editMode));
        on(WeatherChangeEvent.class, (EventListener<WeatherChangeEvent>) event -> event.setCancelled(true));
        on(LeavesDecayEvent.class, (EventListener<LeavesDecayEvent>) event -> event.setCancelled(true));
        on(PlayerDropItemEvent.class, (EventListener<PlayerDropItemEvent>) event -> event.setCancelled(!editMode));
        on(PlayerPickupItemEvent.class, (EventListener<PlayerPickupItemEvent>) event -> event.setCancelled(!editMode));
        on(PlayerInteractEvent.class, (EventListener<PlayerInteractEvent>) event -> {
            if (editMode) return;
            final Player player = event.getPlayer();
            event.setCancelled(!editMode);
            if (event.getItem() == null) return;
            if (!event.getItem().getItemMeta().hasDisplayName()) return;
            if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                return;
            if (event.getItem().getItemMeta().getDisplayName().equals("§8» §9Verlassen §8● §7Rechtsklick")) {
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1f);
                player.kickPlayer("");
            } else if (event.getItem().getItemMeta().getDisplayName().equals("§8» §9Mapauswahl §8● §7Rechtsklick")) {
                this.openArenaVoter(player, false, null);
                player.playSound(player.getLocation(), Sound.CHEST_OPEN, 10f, 10f);
            } else if (event.getItem().getItemMeta().getDisplayName().equals("§8» §9Teamauswahl §8● §7Rechtsklick")) {
                openTeamSelector(player, null);
                player.playSound(player.getLocation(), Sound.CHEST_OPEN, 10f, 10f);
            } else if (event.getItem().getItemMeta().getDisplayName().equals("§8» §9Spielstart §8● §7Rechtsklick")) {
                player.chat("/start");
            } else if (event.getItem().getItemMeta().getDisplayName().equals("§8» §9Abstimmung §8● §7Rechtsklick")) {
                Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, "§8» §9Abstimmung");
                inventory.setItem(1, new ItemBuilder(Material.GOLD_INGOT, 1).setDisplayname("§8» §9Gold").setLore("§7Soll die Resource §9Gold §7im Spiel erscheinen?").build());
                inventory.setItem(3, new ItemBuilder(Material.WEB, 1).setDisplayname("§8» §9Cobweb").setLore("§7Darf das Item §9Cobweb §7im Shop erworben werden?").build());
                for (int i = 0; i < inventory.getSize(); i++) {
                    if (inventory.getItem(i) == null)
                        inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, 15).setDisplayname("§r").build());
                }
                player.openInventory(inventory);
                player.playSound(player.getLocation(), Sound.CHEST_OPEN, 10f, 10f);
            }
        });
        on(InventoryClickEvent.class, (EventListener<InventoryClickEvent>) event -> {
            event.setCancelled(true);
            final Player player = (Player) event.getWhoClicked();
            if (event.getClickedInventory() == null)
                return;
            if (event.getCurrentItem() == null) return;
            if (!event.getCurrentItem().hasItemMeta()) return;
            String title = event.getClickedInventory().getTitle();
            final String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
            if (title.equals("§8» §9Teamauswahl")) {
                event.setCancelled(true);
                final String teamDisplayNameRaw = ChatColor.stripColor(displayName.replace("§8» ", ""));
                final TeamManager teamManager = getPlugin().getTeamManager();
                final Team team = teamManager.getTeamByDisplayName(teamDisplayNameRaw);
                if (team != null) {
                    if (teamManager.isTeamFull(team)) {
                        player.sendMessage(prefix + "§7Das Team ist bereits voll.");
                        return;
                    }

                    if (team.getTeamMembers().contains(player)) {
                        player.sendMessage(prefix + "§7Du bist bereits in diesem Team.");
                        return;
                    }
                    teamManager.leaveTeams(player);
                    teamManager.joinTeam(player, team);
                    player.sendMessage(prefix + "§7Du bist nun im Team " + team.getDisplayName() + "§7.");
                    this.openTeamSelector(player, event.getClickedInventory());

                    getPlugin().getScoreboardManager().sendLobbyScoreboard(player, Bukkit.getOnlinePlayers().size());
                }
            } else if (title.equals("§8» §9Forcemap")) {
                if (!displayName.startsWith("§8» §9")) return;
                ArenaManager arenaManager = getPlugin().getArenaManager();
                Arena arena = arenaManager.getArenaByName(displayName.replace("§8» §9", ""));
                if (arena == null) return;
                arenaManager.setCurrentArena(arena);
                arenaManager.setArenaForced(true);
                player.sendMessage(prefix + "§7Du hast auf die Map §9" + arena.getName() + "§7 gesetzt.");
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                PlayerUtils.parseThroughAllPlayer(player1 -> getPlugin().getScoreboardManager().sendLobbyScoreboard(player, lobbyPlayers.size()));
            } else if (title.equals("§8» §9Mapauswahl")) {
                if (!displayName.startsWith("§8» §9")) return;
                ArenaManager arenaManager = getPlugin().getArenaManager();
                Arena arena = arenaManager.getArenaByName(displayName.replace("§8» §9", ""));
                if (arena == null) return;
                arenaManager.removeVotes(player);
                arena.getVoters().add(player);
                player.sendMessage(prefix + "§7Du hast für die Map §9" + arena.getName() + "§7 abgestimmt.");
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
                this.openArenaVoter(player, false, event.getClickedInventory());
            } else if (title.equals("§8» §9Abstimmung")) {
                event.setCancelled(true);

                ItemStack item = event.getClickedInventory().getItem(2);
                Inventory inventory = event.getClickedInventory();
                ItemStack currentItem = event.getCurrentItem().clone();
                if (!displayName.startsWith("§8» §9")) return;
                if (item == null || item.getType().equals(Material.STAINED_GLASS_PANE)) {
                }
                if (displayName.equals("§8» §9Cobweb") || displayName.equals("§8» §9Gold")) {
                    for (int i = 0; i < inventory.getSize(); i++) {
                        inventory.setItem(i, null);
                    }
                    inventory.setItem(2, new ItemBuilder(currentItem).setDisplayname("§a" + currentItem.getItemMeta().getDisplayName()).build());
                    if (displayName.equals("§8» §9Gold")) {
                        inventory.setItem(1, new ItemBuilder(Material.INK_SACK, withGold.size(), 10).setDisplayname("§8» §9Aktiviert").build());
                        inventory.setItem(3, new ItemBuilder(Material.INK_SACK, withoutGold.size(), 1).setDisplayname("§8» §9Deaktiviert").build());
                    } else {
                        inventory.setItem(1, new ItemBuilder(Material.INK_SACK, withCobweb.size(), 10).setDisplayname("§8» §9Aktiviert").build());
                        inventory.setItem(3, new ItemBuilder(Material.INK_SACK, withoutCobweb.size(), 1).setDisplayname("§8» §9Deaktiviert").build());
                    }
                    for (int i = 0; i < inventory.getSize(); i++) {
                        if (inventory.getItem(i) == null)
                            inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, 15).setDisplayname("§r").build());
                    }
                    player.playSound(player.getLocation(), Sound.CLICK, 1f, 1f);
                    return;
                }
                if (item.getItemMeta().getDisplayName().equals("§a§8» §9Gold")) {
                    if (displayName.equals("§8» §9Aktiviert")) {
                        if (!withGold.contains(player))
                            withGold.add(player);
                        withoutGold.remove(player);
                    } else {
                        if (!withoutGold.contains(player))
                            withoutGold.add(player);
                        withGold.remove(player);
                    }

                    player.playSound(player.getLocation(), Sound.CLICK, 1f, 1f);
                    player.closeInventory();
                } else if (item.getItemMeta().getDisplayName().equals("§a§8» §9Cobweb")) {
                    if (displayName.equals("§8» §9Aktiviert")) {
                        if (!withCobweb.contains(player))
                            withCobweb.add(player);
                        withoutCobweb.remove(player);
                    } else {
                        if (!withoutCobweb.contains(player))
                            withoutCobweb.add(player);
                        withCobweb.remove(player);
                    }
                    player.playSound(player.getLocation(), Sound.CLICK, 1f, 1f);
                    player.closeInventory();
                }
            }
        });
        on(EntityDamageByEntityEvent.class, (EventListener<EntityDamageByEntityEvent>) event -> event.setCancelled(!editMode));
        on(PlayerBedEnterEvent.class, (EventListener<PlayerBedEnterEvent>) event -> event.setCancelled(true));
        on(PlayerArmorStandManipulateEvent.class, (EventListener<PlayerArmorStandManipulateEvent>) event -> event.setCancelled(!editMode));
        on(CreatureSpawnEvent.class, (EventListener<CreatureSpawnEvent>) event -> event.setCancelled(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM && !editMode));
    }

    private void openArenaVoter(Player player, boolean forceMap, Inventory inventory) {
        List<Arena> arenas = getPlugin().getArenaManager().getArenas();
        if (inventory == null)
            inventory = Bukkit.createInventory(null, (this.getSlotCountBySize(arenas.size()) + 2) * 9, forceMap ? "§8» §9Forcemap" : "§8» §9Mapauswahl");
        else for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, null);
        new InventoryFill(inventory).fillSidesWithItem(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, 15).setDisplayname("§r").build());
        Inventory finalInventory = inventory;
        arenas.forEach(arena ->
                finalInventory.addItem(new ItemBuilder(Material.MAP, arena.getVoters().size()).addItemFlags(ItemFlag.HIDE_ATTRIBUTES).setLore(" §7Builder §8» §9" + arena.getBuilder()).setDisplayname("§8» §9" + arena.getName()).build()));
        player.openInventory(inventory);
    }

    private int getSlotCountBySize(int size) {
        int slots = 0;
        while (size > 0) {
            slots++;
            size -= 7;
        }
        return slots;
    }

    private void openTeamSelector(Player player, Inventory inventory) {
        List<Team> teams = getPlugin().getTeamManager().getTeams();
        if (inventory == null) {
            inventory = Bukkit.createInventory(null, (teams.size() == 8 ? 36 : 27), "§8» §9Teamauswahl");
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, 15).setDisplayname("§r").build());
        }
        Integer[] integers = null;
        switch (teams.size()) {
            case 2:
                integers = new Integer[]{ 11, 15 };
                break;
            case 4:
                integers = new Integer[]{ 10, 12, 14, 16 };
                break;
            case 6:
                integers = new Integer[]{ 10, 11, 12, 14, 15, 16 };
                break;
            case 8:
                integers = new Integer[]{ 10, 12, 14, 16, 19, 21, 23, 25 };
                break;
            default:
                integers = new Integer[]{};
                break;
        }

        for (int i = 0; i < teams.size(); i++) {
            final Team team = teams.get(i);
            final TeamColor teamColor = getPlugin().getTeamManager().getColorByDisplayName(team.getDisplayName());
            List<String> memberName = new ArrayList<>();
            for (int y = 0; y < team.getTeamMembers().size(); y++) {
                memberName.add("§9◆ §7" + team.getTeamMembers().get(y).getName());
            }
            inventory.setItem(integers.length == teams.size() ? integers[i] : i + 9, new ItemBuilder(Material.WOOL, 1, teamColor.getColorByte()).setLore(memberName).setDisplayname("§8» §" + teamColor.getKey() + teamColor.getDisplayName()).build());
        }

        player.openInventory(inventory);
    }

    @Override
    public void stop() {
        getPlugin().getSettingsManager().setGold(withGold.size() >= withoutGold.size());
        getPlugin().getSettingsManager().setCobweb(withCobweb.size() >= withoutCobweb.size());
    }
}
