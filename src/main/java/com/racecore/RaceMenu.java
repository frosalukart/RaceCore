package com.racecore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RaceMenu {
    public enum Type { MAIN, RACES, CONFIRM_REMOVE, ADMIN_PLAYERS, ADMIN_RACES }

    public static class Holder implements InventoryHolder {
        public final Type type;
        public final UUID target;
        public Holder(Type type, UUID target) { this.type = type; this.target = target; }
        @Override public Inventory getInventory() { return null; }
    }

    private static ItemStack item(Material m, String name, String... lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(RaceCore.color(name));
        List<String> l = new ArrayList<>();
        for (String s : lore) l.add(RaceCore.color(s));
        meta.setLore(l);
        it.setItemMeta(meta);
        return it;
    }

    private static void fill(Inventory inv) {
        ItemStack g = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) if (inv.getItem(i) == null) inv.setItem(i, g);
    }

    private static void later(RaceCore plugin, Runnable r) { Bukkit.getScheduler().runTask(plugin, r); }

    private static ItemStack raceItem(Race r, boolean current) {
        ItemStack it = new ItemStack(r.icon);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(RaceCore.color(r.display));
        List<String> lore = new ArrayList<>();
        lore.add(RaceCore.color("&8" + r.rarity + (r.rollable ? "" : " &8(admin/crate saja)")));
        lore.add("");
        for (String line : r.description) lore.add(RaceCore.color(line));
        if (current) {
            lore.add("");
            lore.add(RaceCore.color("&a&lRAS KAMU"));
            meta.setEnchantmentGlintOverride(true);
        }
        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    public static void openMain(RaceCore plugin, Player p) {
        Inventory inv = Bukkit.createInventory(new Holder(Type.MAIN, null), 54, RaceCore.color("&8&lRaceCore Menu"));
        Race r = plugin.data().raceOf(p.getUniqueId());
        DataManager.PlayerData pd = plugin.data().get(p.getUniqueId());
        int bal = pd.rerollBalance;
        int pity = pd.pityCount;
        double cost = plugin.getConfig().getDouble("economy.reroll-cost", 5000000);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        sm.setOwningPlayer(p);
        sm.setDisplayName(RaceCore.color("&e&l" + p.getName()));
        List<String> lore = new ArrayList<>();
        lore.add(RaceCore.color("&7Ras: " + r.display));
        lore.add(RaceCore.color("&7Rarity: &f" + r.rarity));
        lore.add(RaceCore.color("&7Reroll Balance: &e" + bal));
        lore.add(RaceCore.color("&7Pity Counter: &d" + pity + "/10 " + (pity >= 10 ? "&a(HOKI BOOST!)" : "")));
        lore.add("");
        for (String line : r.description) lore.add(RaceCore.color(line));
        sm.setLore(lore);
        head.setItemMeta(sm);
        inv.setItem(4, head);

        inv.setItem(20, item(Material.ENDER_EYE, "&a&lRoll Ras",
                "&7Reroll kamu: &e" + bal, "&7Pity saat ini: &d" + pity, "", "&eKlik untuk acak ras baru"));
        inv.setItem(22, item(Material.BOOK, "&b&lDaftar Ras",
                "&7Lihat semua ras dan kemampuannya", "", "&eKlik untuk membuka"));
        inv.setItem(24, item(Material.GOLD_INGOT, "&6&lBeli Reroll",
                "&7Harga: &e" + String.format("%,.0f", cost), "", "&eKlik untuk membeli 1 reroll"));
        inv.setItem(40, item(Material.BARRIER, "&c&lHapus Ras",
                "&7Kembali menjadi Human", "", "&eKlik untuk konfirmasi"));
        if (p.hasPermission("racecore.admin")) {
            inv.setItem(53, item(Material.COMMAND_BLOCK, "&d&lAdmin Panel",
                    "&7Ubah ras pemain online", "", "&eKlik untuk membuka"));
        }
        fill(inv);
        p.openInventory(inv);
    }

    public static void openRaces(RaceCore plugin, Player p) {
        Inventory inv = Bukkit.createInventory(new Holder(Type.RACES, null), 54, RaceCore.color("&8&lDaftar Ras"));
        Race mine = plugin.data().raceOf(p.getUniqueId());
        int slot = 0;
        for (Race r : plugin.races().all()) {
            if (slot >= 45) break;
            inv.setItem(slot++, raceItem(r, r.id.equals(mine.id)));
        }
        inv.setItem(49, item(Material.ARROW, "&e&lKembali"));
        fill(inv);
        p.openInventory(inv);
    }

    public static void openConfirm(RaceCore plugin, Player p) {
        Inventory inv = Bukkit.createInventory(new Holder(Type.CONFIRM_REMOVE, null), 27, RaceCore.color("&8&lHapus Ras?"));
        inv.setItem(11, item(Material.LIME_CONCRETE, "&a&lYA, hapus", "&7Ras kamu jadi Human"));
        inv.setItem(15, item(Material.RED_CONCRETE, "&c&lBATAL"));
        fill(inv);
        p.openInventory(inv);
    }

    public static void openAdminPlayers(RaceCore plugin, Player p) {
        Inventory inv = Bukkit.createInventory(new Holder(Type.ADMIN_PLAYERS, null), 54, RaceCore.color("&8&lAdmin: Pilih Pemain"));
        int slot = 0;
        for (Player t : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break;
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            sm.setOwningPlayer(t);
            sm.setDisplayName(RaceCore.color("&e" + t.getName()));
            Race r = plugin.data().raceOf(t.getUniqueId());
            List<String> lore = new ArrayList<>();
            lore.add(RaceCore.color("&7Ras: " + r.display));
            lore.add("");
            lore.add(RaceCore.color("&eKlik untuk pilih ras"));
            sm.setLore(lore);
            head.setItemMeta(sm);
            inv.setItem(slot++, head);
        }
        inv.setItem(49, item(Material.ARROW, "&e&lKembali"));
        fill(inv);
        p.openInventory(inv);
    }

    public static void openAdminRaces(RaceCore plugin, Player p, UUID target) {
        OfflinePlayer t = Bukkit.getOfflinePlayer(target);
        String name = t.getName() == null ? "?" : t.getName();
        Inventory inv = Bukkit.createInventory(new Holder(Type.ADMIN_RACES, target), 54, RaceCore.color("&8&lSet ras: " + name));
        Race cur = plugin.data().raceOf(target);
        int slot = 0;
        for (Race r : plugin.races().all()) {
            if (slot >= 45) break;
            inv.setItem(slot++, raceItem(r, r.id.equals(cur.id)));
        }
        inv.setItem(49, item(Material.ARROW, "&e&lKembali"));
        fill(inv);
        p.openInventory(inv);
    }

    public static void click(RaceCore plugin, Player p, Holder h, int slot, ItemStack clicked) {
        if (clicked == null) return;
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1f);
        switch (h.type) {
            case MAIN:
                if (slot == 20) { plugin.rerollNow(p); later(plugin, () -> openMain(plugin, p)); }
                else if (slot == 22) later(plugin, () -> openRaces(plugin, p));
                else if (slot == 24) { plugin.buyReroll(p); later(plugin, () -> openMain(plugin, p)); }
                else if (slot == 40) later(plugin, () -> openConfirm(plugin, p));
                else if (slot == 53 && p.hasPermission("racecore.admin")) later(plugin, () -> openAdminPlayers(plugin, p));
                break;
            case RACES:
                if (slot == 49) later(plugin, () -> openMain(plugin, p));
                break;
            case CONFIRM_REMOVE:
                if (slot == 11) {
                    plugin.setRace(p, plugin.races().human());
                    p.sendMessage(plugin.msg("removed"));
                    later(plugin, () -> openMain(plugin, p));
                } else if (slot == 15) {
                    later(plugin, () -> openMain(plugin, p));
                }
                break;
            case ADMIN_PLAYERS:
                if (!p.hasPermission("racecore.admin")) return;
                if (slot == 49) { later(plugin, () -> openMain(plugin, p)); break; }
                if (clicked.getType() == Material.PLAYER_HEAD && clicked.getItemMeta() instanceof SkullMeta sm
                        && sm.getOwningPlayer() != null) {
                    UUID target = sm.getOwningPlayer().getUniqueId();
                    later(plugin, () -> openAdminRaces(plugin, p, target));
                }
                break;
            case ADMIN_RACES:
                if (!p.hasPermission("racecore.admin")) return;
                if (slot == 49) { later(plugin, () -> openAdminPlayers(plugin, p)); break; }
                List<Race> list = new ArrayList<>(plugin.races().all());
                if (slot >= 0 && slot < list.size() && slot < 45) {
                    Race r = list.get(slot);
                    OfflinePlayer t = Bukkit.getOfflinePlayer(h.target);
                    plugin.setRace(t, r);
                    p.sendMessage(plugin.msg("admin-set", "{player}", t.getName() == null ? "?" : t.getName(),
                            "{race}", RaceCore.color(r.display)));
                    later(plugin, () -> openAdminRaces(plugin, p, h.target));
                }
                break;
        }
    }
}
