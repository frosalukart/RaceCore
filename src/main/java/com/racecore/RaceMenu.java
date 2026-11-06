package com.racecore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RaceMenu {
    public static class Holder implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }

    public static void open(RaceCore plugin, Player p) {
        int n = plugin.races().all().size();
        int size = Math.min(54, Math.max(9, ((n + 8) / 9) * 9));
        Inventory inv = Bukkit.createInventory(new Holder(), size, RaceCore.color("&8&lRaces"));
        Race mine = plugin.data().raceOf(p.getUniqueId());
        int slot = 0;
        for (Race r : plugin.races().all()) {
            if (slot >= size) break;
            ItemStack it = new ItemStack(r.icon);
            ItemMeta meta = it.getItemMeta();
            meta.setDisplayName(RaceCore.color(r.display));
            List<String> lore = new ArrayList<>();
            lore.add(RaceCore.color("&8" + r.rarity + (r.rollable ? "" : " &8(admin/crate only)")));
            lore.add("");
            for (String line : r.description) lore.add(RaceCore.color(line));
            if (r.id.equals(mine.id)) {
                lore.add("");
                lore.add(RaceCore.color("&a&lYOUR RACE"));
                meta.setEnchantmentGlintOverride(true);
            }
            meta.setLore(lore);
            it.setItemMeta(meta);
            inv.setItem(slot++, it);
        }
        p.openInventory(inv);
    }
}
