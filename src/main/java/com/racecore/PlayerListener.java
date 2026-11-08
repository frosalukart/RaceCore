package com.racecore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {
    private final RaceCore plugin;

    public PlayerListener(RaceCore plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        DataManager.PlayerData d = plugin.data().get(p.getUniqueId());
        if (!d.freeRollUsed) {
            d.freeRollUsed = true;
            d.rerollBalance += 1;
            plugin.data().save();
            p.sendMessage(plugin.msg("first-join"));
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> { if (p.isOnline()) plugin.abilities().apply(p); }, 5L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.abilities().apply(p), 2L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.abilities().release(e.getPlayer(), null);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof RaceMenu.Holder h) {
            e.setCancelled(true);
            if (e.getClickedInventory() != null && e.getClickedInventory().equals(e.getView().getTopInventory())
                    && e.getWhoClicked() instanceof Player p) {
                RaceMenu.click(plugin, p, h, e.getSlot(), e.getCurrentItem());
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof RaceMenu.Holder) e.setCancelled(true);
    }
}
