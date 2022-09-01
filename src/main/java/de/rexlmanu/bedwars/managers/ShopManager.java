/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.managers;

import com.google.common.collect.Maps;
import lombok.Data;
import de.rexlmanu.bedwars.ManagerPlugin;
import de.rexlmanu.bedwars.entities.Team;
import de.rexlmanu.bedwars.utils.ItemBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 01.07.2019 / 11:55                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public final class ShopManager implements Listener {

    private static ManagerPlugin plugin;
    private List<ShopCategory> categories;
    private ItemStack glassStack;
    private String prefix;

    public ShopManager(ManagerPlugin plugin) {
        ShopManager.plugin = plugin;
        this.categories = new ArrayList<>();
        this.glassStack = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, 15).setDisplayname("§r").build();
        this.prefix = plugin.getSettingsManager().getPrefix();

        this.registerCategories();

        Bukkit.getPluginManager().registerEvents(this, ShopManager.plugin);
    }

    public void openShop(Player player, Inventory inventory, ShopCategory currentCategory) {
        if (inventory == null) inventory = Bukkit.createInventory(null, 4 * 9, "§8» §9Shop");
        for (int i = 0; i < categories.size(); i++)
            inventory.setItem((i) + ((9 - categories.size()) / 2), categories.get(i).getDisplayItem());
        for (int i = 0; i < 9; i++) inventory.setItem(i + 9, this.glassStack);
        if (currentCategory != null) {
            for (int i = 0; i < 18; i++) inventory.setItem(i + 18, null);
            for (int i = 0; i < currentCategory.getProducts().size(); i++) {
                ShopProduct shopProduct = currentCategory.getProducts().get(i);
                if (! shopProduct.getBuyProduct().getType().equals(Material.WEB) || plugin.getSettingsManager().isCobweb()) {
                    inventory.setItem(i + 18, this.addLore(shopProduct.getBuyProduct().clone(), "§r",
                            "§8» §7" + shopProduct.getSpawnerType().getTranslation() + " §8◆ §7" + shopProduct.getPrice()));
                }
            }
        }
        player.openInventory(inventory);
    }

    @EventHandler
    public void handle(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;
        if (clickedInventory.getTitle() == null) return;
        if (! clickedInventory.getTitle().equals("§8» §9Shop")) return;
        event.setCancelled(true);
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) return;
        Player player = (Player) event.getWhoClicked();
        if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
            Optional<ShopCategory> category = this.categories.stream().filter(shopCategory ->
                    shopCategory.getDisplayItem().getItemMeta().getDisplayName().equals(currentItem.getItemMeta().getDisplayName()))
                    .findFirst();
            category.ifPresent(shopCategory -> this.openShop(player, clickedInventory, shopCategory));
            if (category.isPresent()) return;
        }

        ItemStack stack = currentItem.clone();
        ItemMeta itemMeta = stack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(null);
            stack.setItemMeta(itemMeta);
        }
        this.categories.forEach(shopCategory ->
                shopCategory.getProducts().stream().filter(shopProduct ->
                        shopProduct.getBuyProduct().equals(stack)).findFirst().ifPresent(shopProduct -> {

                    final boolean shiftClick = event.isShiftClick();

                    boolean firstTime = false;

                    final int amount = shiftClick ? 64 / shopProduct.getBuyProduct().getAmount() : 1;
                    PlayerInventory playerInventory = player.getInventory();
                    for (int i = 0; i < amount; i++) {

                        if (playerInventory.containsAtLeast(new ItemStack(shopProduct.getSpawnerType().getMaterial()), shopProduct.getPrice())) {
                            removeItems(playerInventory, shopProduct.getSpawnerType().getMaterial(), shopProduct.getPrice());
                            shopProduct.buyItem(player);
                            if (! firstTime) {
                                plugin.getLocaleQuery().sendMessage(player, prefix + "§7Du hast erfolgreich §9" + ChatColor.BLUE + "<item>§7 gekauft.", shopProduct.getBuyProduct().getType(), shopProduct.getBuyProduct().getDurability(), null);
                                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                                firstTime = true;
                            }
                        } else {
                            if (! shiftClick) {
                                player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 1f);
                                player.sendMessage(prefix + "§7Du besitzt §cnicht §7genug Ressourcen.");
                            }
                            break;
                        }
                    }

                }));
    }

    private ItemStack addLore(ItemStack itemStack, String... lores) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(Arrays.asList(lores));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void registerCategories() {
        this.categories.addAll(Arrays.asList(
                new ShopCategory(new ItemBuilder(Material.SANDSTONE).setDisplayname("§9Blöcke").build(), Arrays.asList(
                        new ShopProduct(new ItemBuilder(Material.SANDSTONE, 4).build(), SpawnerManager.SpawnerType.BRONZE, 1),
                        new ShopProduct(new ItemBuilder(Material.EMERALD_BLOCK, 1).build(), SpawnerManager.SpawnerType.IRON, 3),
                        new ShopProduct(new ItemBuilder(Material.GLASS, 1).build(), SpawnerManager.SpawnerType.BRONZE, 1),
                        new TrapBlockShopProduct(new ItemBuilder(Material.CLAY, 1).build(), SpawnerManager.SpawnerType.IRON, 1, plugin),
                        new ShopProduct(new ItemBuilder(Material.GLOWSTONE, 1).build(), SpawnerManager.SpawnerType.BRONZE, 2),
                        new ShopProduct(new ItemBuilder(Material.WEB, 1).build(), SpawnerManager.SpawnerType.BRONZE, 16)
                )),
                new ShopCategory(new ItemBuilder(Material.GOLD_SWORD).setDisplayname("§9Waffen").build(), Arrays.asList(
                        new ShopProduct(new ItemBuilder(Material.STICK).addEnchant(Enchantment.KNOCKBACK).build(), SpawnerManager.SpawnerType.BRONZE, 10),
                        new ShopProduct(new ItemBuilder(Material.GOLD_SWORD).addEnchant(Enchantment.DAMAGE_ALL).build(), SpawnerManager.SpawnerType.IRON, 1),
                        new ShopProduct(new ItemBuilder(Material.GOLD_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 2).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).build(), SpawnerManager.SpawnerType.IRON, 3),
                        new ShopProduct(new ItemBuilder(Material.GOLD_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 3).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).build(), SpawnerManager.SpawnerType.IRON, 7),
                        new ShopProduct(new ItemBuilder(Material.IRON_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 1).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).build(), SpawnerManager.SpawnerType.GOLD, 3)
                )),
                new ShopCategory(new ItemBuilder(Material.BOW).setDisplayname("§9Bögen").build(), Arrays.asList(
                        new ShopProduct(new ItemBuilder(Material.BOW).build(), SpawnerManager.SpawnerType.GOLD, 3),
                        new ShopProduct(new ItemBuilder(Material.BOW).addEnchant(Enchantment.ARROW_DAMAGE, 1).build(), SpawnerManager.SpawnerType.GOLD, 5),
                        new ShopProduct(new ItemBuilder(Material.BOW).addEnchant(Enchantment.ARROW_DAMAGE, 2).addEnchant(Enchantment.ARROW_KNOCKBACK, 1).build(), SpawnerManager.SpawnerType.GOLD, 9),
                        new ShopProduct(new ItemBuilder(Material.BOW).addEnchant(Enchantment.ARROW_DAMAGE, 2).addEnchant(Enchantment.ARROW_KNOCKBACK, 2).addEnchant(Enchantment.ARROW_FIRE).build(), SpawnerManager.SpawnerType.GOLD, 15),
                        new ShopProduct(new ItemBuilder(Material.ARROW, 2).build(), SpawnerManager.SpawnerType.BRONZE, 5)
                )),
                new ShopCategory(new ItemBuilder(Material.WOOD_PICKAXE).setDisplayname("§9Werkzeuge").build(), Arrays.asList(
                        new ShopProduct(new ItemBuilder(Material.WOOD_PICKAXE).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).addEnchant(Enchantment.DIG_SPEED, 1).build(), SpawnerManager.SpawnerType.BRONZE, 4),
                        new ShopProduct(new ItemBuilder(Material.STONE_PICKAXE).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).addEnchant(Enchantment.DIG_SPEED, 1).build(), SpawnerManager.SpawnerType.IRON, 2),
                        new ShopProduct(new ItemBuilder(Material.IRON_PICKAXE).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).addEnchant(Enchantment.DIG_SPEED, 1).build(), SpawnerManager.SpawnerType.GOLD, 1),
                        new ShopProduct(new ItemBuilder(Material.FISHING_ROD).build(), SpawnerManager.SpawnerType.IRON, 8),
                        new ShopProduct(new ItemBuilder(Material.FLINT_AND_STEEL).build(), SpawnerManager.SpawnerType.IRON, 3)
                )),
                new ShopCategory(new ItemBuilder(Material.COOKED_BEEF).setDisplayname("§9Essen").build(), Arrays.asList(
                        new ShopProduct(new ItemBuilder(Material.COOKIE, 2).build(), SpawnerManager.SpawnerType.BRONZE, 1),
                        new ShopProduct(new ItemBuilder(Material.COOKED_FISH, 2).build(), SpawnerManager.SpawnerType.BRONZE, 4),
                        new ShopProduct(new ItemBuilder(Material.CAKE, 1).build(), SpawnerManager.SpawnerType.IRON, 1),
                        new ShopProduct(new ItemBuilder(Material.GOLDEN_APPLE, 1).build(), SpawnerManager.SpawnerType.GOLD, 1)
                )),
                new ShopCategory(new ItemBuilder(Material.LEATHER_CHESTPLATE).setDisplayname("§9Rüstung").build(), Arrays.asList(
                        new LeatherArmorShopProduct(new ItemBuilder(Material.LEATHER_HELMET).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).build(), SpawnerManager.SpawnerType.BRONZE, 1),
                        new LeatherArmorShopProduct(new ItemBuilder(Material.LEATHER_LEGGINGS).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).build(), SpawnerManager.SpawnerType.BRONZE, 1),
                        new LeatherArmorShopProduct(new ItemBuilder(Material.LEATHER_BOOTS).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).build(), SpawnerManager.SpawnerType.BRONZE, 1),
                        new ShopProduct(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).build(), SpawnerManager.SpawnerType.IRON, 1),
                        new ShopProduct(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).build(), SpawnerManager.SpawnerType.IRON, 3),
                        new ShopProduct(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3).setUnbreakable(true).addItemFlags(ItemFlag.HIDE_UNBREAKABLE).build(), SpawnerManager.SpawnerType.IRON, 7)
                )),
                new ShopCategory(new ItemBuilder(Material.POTION).setDisplayname("§9Tränke").build(), Arrays.asList(
                        new ShopProduct(new ItemBuilder(Material.POTION, 1, 8194).build(), SpawnerManager.SpawnerType.IRON, 5),
                        new ShopProduct(new ItemBuilder(Material.POTION, 1, 8261).build(), SpawnerManager.SpawnerType.IRON, 2),
                        new ShopProduct(new ItemBuilder(Material.POTION, 1, 8229).build(), SpawnerManager.SpawnerType.IRON, 5),
                        new ShopProduct(new ItemBuilder(Material.POTION, 1, 8201).build(), SpawnerManager.SpawnerType.GOLD, 10),
                        new ShopProduct(new ItemBuilder(Material.POTION, 1, 8194).build(), SpawnerManager.SpawnerType.IRON, 8),
                        new ShopProduct(new ItemBuilder(Material.POTION, 1, 16456).build(), SpawnerManager.SpawnerType.IRON, 16),
                        new ShopProduct(new ItemBuilder(Material.POTION, 1, 16458).build(), SpawnerManager.SpawnerType.IRON, 10)
                )),
                new ShopCategory(new ItemBuilder(Material.EMERALD).setDisplayname("§9Special").build(), Arrays.asList(
                        new ShopProduct(new ItemBuilder(Material.TNT).build(), SpawnerManager.SpawnerType.GOLD, 2),
                        new ChestShopProduct(new ItemBuilder(Material.ENDER_CHEST).build(), SpawnerManager.SpawnerType.GOLD, 1, true, false, true, 4),
                        new ChestShopProduct(new ItemBuilder(Material.CHEST).build(), SpawnerManager.SpawnerType.IRON, 1, false, false, false, 3),
                        new ShopProduct(new ItemBuilder(Material.ENDER_PEARL, 2).build(), SpawnerManager.SpawnerType.GOLD, 13),
                        new ShopProduct(new ItemBuilder(Material.LADDER, 2).build(), SpawnerManager.SpawnerType.BRONZE, 4)
                ))
        ));
    }

    @Data
    public class ShopCategory {

        private ItemStack displayItem;
        private List<ShopProduct> products;

        public ShopCategory(ItemStack displayItem, List<ShopProduct> products) {
            this.displayItem = displayItem;
            this.products = products;
        }
    }

    @Data
    public class ShopProduct {

        private ItemStack buyProduct;
        private SpawnerManager.SpawnerType spawnerType;
        private int price;
        private boolean shiftBuy;

        public ShopProduct(ItemStack buyProduct, SpawnerManager.SpawnerType spawnerType, int price) {
            this.buyProduct = buyProduct;
            this.spawnerType = spawnerType;
            this.price = price;
            this.shiftBuy = true;
        }

        public ShopProduct(ItemStack buyProduct, SpawnerManager.SpawnerType spawnerType, int price, boolean shiftBuy) {
            this.buyProduct = buyProduct;
            this.spawnerType = spawnerType;
            this.price = price;
            this.shiftBuy = shiftBuy;
        }

        public void buyItem(Player player) {
            player.getInventory().addItem(this.buyProduct);
        }
    }

    public class LeatherArmorShopProduct extends ShopProduct {

        public LeatherArmorShopProduct(ItemStack buyProduct, SpawnerManager.SpawnerType spawnerType, int price) {
            super(buyProduct, spawnerType, price);
        }

        public LeatherArmorShopProduct(ItemStack buyProduct, SpawnerManager.SpawnerType spawnerType, int price, boolean shiftBuy) {
            super(buyProduct, spawnerType, price, shiftBuy);
        }

        @Override
        public void buyItem(Player player) {
            ItemStack itemStack = getBuyProduct().clone();
            Team team = plugin.getTeamManager().getTeamByPlayer(player);
            if (team != null) {
                LeatherArmorMeta itemMeta = (LeatherArmorMeta) itemStack.getItemMeta();
                itemMeta.setColor(plugin.getTeamManager().getColorByDisplayName(team.getDisplayName()).getColor());
                itemStack.setItemMeta(itemMeta);
            }
            player.getInventory().addItem(itemStack);
        }
    }

    public class TrapBlockShopProduct extends ShopProduct implements Listener {

        private ManagerPlugin plugin;

        public TrapBlockShopProduct(ItemStack buyProduct, SpawnerManager.SpawnerType spawnerType, int price, ManagerPlugin plugin) {
            super(buyProduct, spawnerType, price);
            this.plugin = plugin;
            Bukkit.getPluginManager().registerEvents(this, this.plugin);
        }

        @EventHandler
        public void handle(PlayerMoveEvent event) {
            Block block = event.getTo().clone().subtract(0, 1, 0).getBlock();
            if (! block.getType().equals(getBuyProduct().getType())) return;
            Player player = event.getPlayer();
            Team team = plugin.getTeamManager().getTeamByPlayer(player);
            if (team == null) return;
            if (! block.hasMetadata("team")) return;
            String teamDisplayName = block.getMetadata("team").get(0).asString();
            if (! team.getDisplayName().equalsIgnoreCase(teamDisplayName)) {
                block.setType(Material.AIR);
                player.playSound(player.getLocation(), Sound.EXPLODE, 1f, 1f);
                return;
            }
        }

        @EventHandler
        public void handle(BlockPlaceEvent event) {
            Player player = event.getPlayer();
            Team team = plugin.getTeamManager().getTeamByPlayer(player);
            if (team == null) {
                event.setBuild(false);
                event.setCancelled(true);
                return;
            }
            Block block = event.getBlock();
            block.setMetadata("team", new FixedMetadataValue(plugin, team.getDisplayName()));
        }
    }

    public class ChestShopProduct extends ShopProduct implements Listener {

        private boolean teamChest, explosionOnBreak, slowBreak;
        private int inventorySize;
        private Map<Location, Inventory> inventories;

        public ChestShopProduct(ItemStack buyProduct, SpawnerManager.SpawnerType spawnerType, int price, boolean teamChest, boolean explosionOnBreak, boolean slowBreak, int inventorySize) {
            super(buyProduct, spawnerType, price);
            this.teamChest = teamChest;
            this.explosionOnBreak = explosionOnBreak;
            this.slowBreak = slowBreak;
            this.inventorySize = inventorySize;
            this.inventories = Maps.newHashMap();
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        @EventHandler
        public void handle(PlayerInteractEvent event) {
            Block clickedBlock = event.getClickedBlock();
            if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                if (! inventories.containsKey(clickedBlock.getLocation())) return;
                if (clickedBlock.hasMetadata("slowBreak") && clickedBlock.hasMetadata("team")) {
                    if (clickedBlock.getMetadata("slowBreak").get(0).asBoolean()) {
                        Team chestTeam = plugin.getTeamManager().getTeamByDisplayName(clickedBlock.getMetadata("team").get(0).asString());
                        Team team = plugin.getTeamManager().getTeamByPlayer(event.getPlayer());
                        if (team == null) return;
                        if (! team.equals(chestTeam)) {
                            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20, 2, false, false));
                        }
                    }
                }
                return;
            }
            if (clickedBlock == null) return;
            if (inventories.containsKey(clickedBlock.getLocation())) {
                event.setCancelled(true);

                if (clickedBlock.hasMetadata("teamChest") && clickedBlock.hasMetadata("team")) {
                    if (clickedBlock.getMetadata("teamChest").get(0).asBoolean()) {
                        Team chestTeam = plugin.getTeamManager().getTeamByDisplayName(clickedBlock.getMetadata("team").get(0).asString());
                        event.getPlayer().openInventory(chestTeam.getTeamChest());
                    } else event.getPlayer().openInventory(inventories.get(clickedBlock.getLocation()));
                } else
                    event.getPlayer().openInventory(inventories.get(clickedBlock.getLocation()));
            }
        }

        @EventHandler
        public void handle(BlockPlaceEvent event) {
            if (getBuyProduct().equals(event.getItemInHand())) {
                Block block = event.getBlock();
                this.inventories.put(block.getLocation(), Bukkit.createInventory(null, this.inventorySize * 9, "§8» §9§lKiste"));
                block.setMetadata("explosionOnBreak", new FixedMetadataValue(plugin, this.explosionOnBreak));
                block.setMetadata("slowBreak", new FixedMetadataValue(plugin, this.slowBreak));
                block.setMetadata("teamChest", new FixedMetadataValue(plugin, this.teamChest));
                Team team = plugin.getTeamManager().getTeamByPlayer(event.getPlayer());
                if (team != null)
                    block.setMetadata("team", new FixedMetadataValue(plugin, team.getDisplayName()));
            }
        }

        @EventHandler
        public void handle(BlockBreakEvent event) {
            Block block = event.getBlock();
            if (block.hasMetadata("explosionOnBreak") && block.hasMetadata("team")) {
                if (block.getMetadata("explosionOnBreak").get(0).asBoolean()) {
                    Team chestTeam = plugin.getTeamManager().getTeamByDisplayName(block.getMetadata("team").get(0).asString());
                    Team team = plugin.getTeamManager().getTeamByPlayer(event.getPlayer());
                    event.getBlock().getDrops().clear();
                    if (team == null) return;

                    Inventory chestInventory = inventories.get(block.getLocation());
                    if (chestInventory != null)
                        for (ItemStack content : chestInventory.getContents()) event.getBlock().getDrops().add(content);


                    if (! team.equals(chestTeam)) {
                        block.getWorld().createExplosion(
                                block.getLocation().getX(),
                                block.getLocation().getY(),
                                block.getLocation().getZ(), 2, false, false);
                    }
                }
            }
        }

    }

    public static void removeItems(final Inventory inventory, final Material type, int amount) {
        if (amount <= 0) {
            return;
        }
        final int size = inventory.getSize();
        for (int slot = 0; slot < size; slot++) {
            final ItemStack is = inventory.getItem(slot);
            if (is == null) {
                continue;
            }
            if (type == is.getType()) {
                final int newAmount = is.getAmount() - amount;
                if (newAmount > 0) {
                    is.setAmount(newAmount);
                    break;
                } else {
                    inventory.clear(slot);
                    amount = - newAmount;
                    if (amount == 0) {
                        break;
                    }
                }
            }
        }
    }
}
