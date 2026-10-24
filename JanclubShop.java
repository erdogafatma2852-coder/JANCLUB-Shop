package com.janclub.shop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class JanclubShop extends JavaPlugin implements Listener {

    private Economy economy;
    private NamespacedKey actionKey;
    private NamespacedKey itemKey;

    private final String mainTitle = color("&8ᴊᴀɴᴄʟᴜʙ | &dꜱʜᴏᴘ");
    private final String endTitle = color("&8ꜱʜᴏᴘ | &5END");
    private final String netherTitle = color("&8ꜱʜᴏᴘ | &cNETHER");
    private final String gearTitle = color("&8ꜱʜᴏᴘ | &bGEAR");
    private final String foodTitle = color("&8ꜱʜᴏᴘ | &aFOOD");

    private record ShopItem(Material material, double price, String name) {}

    private final Map<String, LinkedHashMap<String, ShopItem>> categories =
            new LinkedHashMap<>();

    @Override
    public void onEnable() {
        actionKey = new NamespacedKey(this, "action");
        itemKey = new NamespacedKey(this, "item");

        if (!setupEconomy()) {
            getLogger().severe("Vault veya ekonomi sistemi bulunamadi!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        createItems();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("JANCLUB Shop aktif!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> provider =
                getServer().getServicesManager().getRegistration(Economy.class);

        if (provider == null) return false;
        economy = provider.getProvider();
        return economy != null;
    }

    private void add(String category, String id, Material material,
                     double price, String name) {
        categories.computeIfAbsent(category, k -> new LinkedHashMap<>())
                .put(id, new ShopItem(material, price, name));
    }

    private void createItems() {
        // END
        add("end", "enderchest", Material.ENDER_CHEST, 2500, "Ender Sandigi");
        add("end", "end-enderpearl", Material.ENDER_PEARL, 75, "Ender Incisi");
        add("end", "endstone", Material.END_STONE, 8, "End Tasi");
        add("end", "dragonbreath", Material.DRAGON_BREATH, 1000, "Ejderha Nefesi");
        add("end", "endrod", Material.END_ROD, 100, "End Cubugu");
        add("end", "chorusfruit", Material.CHORUS_FRUIT, 10, "Korus Meyvesi");
        add("end", "poppedchorusfruit", Material.POPPED_CHORUS_FRUIT, 15, "Patlamis Korus Meyvesi");
        add("end", "shulkershell", Material.SHULKER_SHELL, 350, "Shulker Kabuğu");
        add("end", "shulkerbox", Material.SHULKER_BOX, 800, "Shulker Kutusu");

        // NETHER
        add("nether", "blazerod", Material.BLAZE_ROD, 150, "Blaze Cubugu");
        add("nether", "netherwart", Material.NETHER_WART, 15, "Nether Wart");
        add("nether", "glowstonedust", Material.GLOWSTONE_DUST, 15, "Isik Tasi Tozu");
        add("nether", "magmacream", Material.MAGMA_CREAM, 15, "Magma Kremi");
        add("nether", "ghasttear", Material.GHAST_TEAR, 350, "Ghast Gozyasi");
        add("nether", "quartz", Material.QUARTZ, 15, "Nether Kuvars");
        add("nether", "soulsand", Material.SOUL_SAND, 50, "Ruh Kumu");
        add("nether", "magmablock", Material.MAGMA_BLOCK, 35, "Magma Blogu");
        add("nether", "cryingobsidian", Material.CRYING_OBSIDIAN, 150, "Aglayan Obsidyen");

        // GEAR — shard, spawner ve patlayici eklenmedi
        add("gear", "obsidian", Material.OBSIDIAN, 100, "Obsidyen");
        add("gear", "glowstone", Material.GLOWSTONE, 100, "Isik Tasi");
        add("gear", "totemofundying", Material.TOTEM_OF_UNDYING, 1250, "Totem");
        add("gear", "gear-enderpearl", Material.ENDER_PEARL, 75, "Ender Incisi");
        add("gear", "gear-goldenapple", Material.GOLDEN_APPLE, 250, "Altin Elma");
        add("gear", "bottleoenchanting", Material.EXPERIENCE_BOTTLE, 100, "Buyu Sis esi");
        add("gear", "arrow", Material.ARROW, 250, "Ok");

        // FOOD
        add("food", "potato", Material.POTATO, 75, "Patates");
        add("food", "sweetberries", Material.SWEET_BERRIES, 50, "Tatli Meyveler");
        add("food", "melonslice", Material.MELON_SLICE, 10, "Karpuz Dilimi");
        add("food", "carrot", Material.CARROT, 65, "Havuc");
        add("food", "apple", Material.APPLE, 25, "Elma");
        add("food", "cookedchicken", Material.COOKED_CHICKEN, 30, "Pisirilmis Tavuk");
        add("food", "steak", Material.COOKED_BEEF, 35, "Biftek");
        add("food", "goldencarrot", Material.GOLDEN_CARROT, 50, "Altin Havuc");
        add("food", "food-goldenapple", Material.GOLDEN_APPLE, 250, "Altin Elma");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyuncular icindir.");
            return true;
        }
        openMain(player);
        return true;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private ItemStack menuItem(Material material, String name,
                               String action, String value, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));

        List<String> lines = new ArrayList<>();
        for (String line : lore) lines.add(color(line));
        meta.setLore(lines);

        meta.getPersistentDataContainer().set(
                actionKey, PersistentDataType.STRING, action);
        if (value != null) {
            meta.getPersistentDataContainer().set(
                    itemKey, PersistentDataType.STRING, value);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void fill(Inventory inventory) {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(color("&8 "));
        pane.setItemMeta(meta);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (i < 9 || i >= inventory.getSize() - 9
                    || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, pane);
            }
        }
    }

    private void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, mainTitle);
        fill(inv);

        inv.setItem(10, menuItem(Material.ENDER_CHEST, "&5&lEND",
                "category", "end", "&7End esyalari", "", "&eTiklayarak ac"));
        inv.setItem(12, menuItem(Material.NETHERRACK, "&c&lNETHER",
                "category", "nether", "&7Nether esyalari", "", "&eTiklayarak ac"));
        inv.setItem(14, menuItem(Material.DIAMOND_CHESTPLATE, "&b&lGEAR",
                "category", "gear", "&7Ekipmanlar", "", "&eTiklayarak ac"));
        inv.setItem(16, menuItem(Material.COOKED_BEEF, "&a&lFOOD",
                "category", "food", "&7Yiyecekler", "", "&eTiklayarak ac"));

        player.openInventory(inv);
    }

    private void openCategory(Player player, String category) {
        String title = switch (category) {
            case "end" -> endTitle;
            case "nether" -> netherTitle;
            case "gear" -> gearTitle;
            case "food" -> foodTitle;
            default -> mainTitle;
        };

        Inventory inv = Bukkit.createInventory(null, 54, title);
        fill(inv);

        LinkedHashMap<String, ShopItem> items = categories.get(category);
        if (items == null) return;

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int index = 0;
        for (Map.Entry<String, ShopItem> entry : items.entrySet()) {
            if (index >= slots.length) break;
            ShopItem item = entry.getValue();

            inv.setItem(slots[index++], menuItem(
                    item.material(),
                    "&f" + item.name(),
                    "buy",
                    entry.getKey(),
                    "&7Fiyat: &e" + format(item.price()) + "$",
                    "",
                    "&aSatın almak için tıkla"
            ));
        }

        inv.setItem(49, menuItem(Material.ARROW, "&cGeri",
                "back", null, "&7Ana menüye dön"));
        player.openInventory(inv);
    }

    private String format(double amount) {
        if (amount == Math.floor(amount)) {
            return String.format(Locale.US, "%.0f", amount);
        }
        return String.format(Locale.US, "%.2f", amount);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.equals(mainTitle) && !title.equals(endTitle)
                && !title.equals(netherTitle) && !title.equals(gearTitle)
                && !title.equals(foodTitle)) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null
                || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        String action = meta.getPersistentDataContainer().get(
                actionKey, PersistentDataType.STRING);
        String value = meta.getPersistentDataContainer().get(
                itemKey, PersistentDataType.STRING);

        if (action == null) return;

        switch (action) {
            case "category" -> openCategory(player, value);
            case "back" -> openMain(player);
            case "buy" -> purchase(player, value);
        }
    }

    private void purchase(Player player, String id) {
        ShopItem selected = null;

        for (Map<String, ShopItem> items : categories.values()) {
            if (items.containsKey(id)) {
                selected = items.get(id);
                break;
            }
        }

        if (selected == null) return;

        if (economy.getBalance(player) < selected.price()) {
            player.sendMessage(color("&8[&dJANCLUB&8] &cYeterli paran yok!"));
            return;
        }

        if (!economy.withdrawPlayer(player, selected.price()).transactionSuccess()) {
            player.sendMessage(color("&8[&dJANCLUB&8] &cPara cekilemedi!"));
            return;
        }

        Map<Integer, ItemStack> leftovers =
                player.getInventory().addItem(new ItemStack(selected.material(), 1));

        if (!leftovers.isEmpty()) {
            economy.depositPlayer(player, selected.price());
            player.sendMessage(color("&8[&dJANCLUB&8] &cEnvanterin dolu! Paran geri verildi."));
            return;
        }

        player.sendMessage(color("&8[&dJANCLUB&8] &a" + selected.name()
                + " satin alindi! &7(-" + format(selected.price()) + "$)"));
        player.playSound(player.getLocation(),
                org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);
    }
}
