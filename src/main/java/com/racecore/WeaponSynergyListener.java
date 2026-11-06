package com.racecore;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WeaponSynergyListener implements Listener {
    private final RaceCore plugin;

    public WeaponSynergyListener(RaceCore plugin) { this.plugin = plugin; }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;

        Race r = plugin.data().raceOf(p.getUniqueId());
        ItemStack weapon = p.getInventory().getItemInMainHand();
        if (weapon == null || !weapon.hasItemMeta()) return;

        ItemMeta meta = weapon.getItemMeta();
        if (!meta.hasDisplayName()) return;

        String name = meta.getDisplayName().toLowerCase();
        String raceId = r.id.toLowerCase();

        // Terra Breaker Synergy (Dwarf / Earth races)
        if (name.contains("terra breaker") && (raceId.contains("dwarf") || raceId.contains("titan") || raceId.contains("golem"))) {
            e.setDamage(e.getDamage() * 1.25); // +25% Extra Synergy Damage
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 1, true, false));
        }

        // Leviathan's Trident Synergy (Mermaid / Ocean races)
        if (name.contains("leviathan") && (raceId.contains("mermaid") || raceId.contains("siren") || raceId.contains("triton"))) {
            e.setDamage(e.getDamage() * 1.25);
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1, true, false));
        }

        // Ignis Core Blade Synergy (Dragonkin / Fire races)
        if (name.contains("ignis") && (raceId.contains("dragon") || raceId.contains("demon") || raceId.contains("fire") || raceId.contains("ignis"))) {
            e.setDamage(e.getDamage() * 1.30);
            e.getEntity().setFireTicks(100);
        }

        // Zephyr Tempest Bow Synergy (Elf / Wind races)
        if (name.contains("zephyr") && (raceId.contains("elf") || raceId.contains("sylph") || raceId.contains("wind"))) {
            e.setDamage(e.getDamage() * 1.20);
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, true, false));
        }

        // Luminance Scepter Synergy (Angel / Holy races)
        if (name.contains("luminance") && (raceId.contains("angel") || raceId.contains("celestial") || raceId.contains("light"))) {
            e.setDamage(e.getDamage() * 1.25);
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, false));
        }

        // Void Scythe Synergy (Vampire / Dark races)
        if (name.contains("void scythe") && (raceId.contains("vampire") || raceId.contains("shadow") || raceId.contains("dark"))) {
            e.setDamage(e.getDamage() * 1.30);
            p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + (e.getFinalDamage() * 0.15))); // 15% Lifesteal
        }

        // Stormcaller Blade Synergy (Thunder / Elemental races)
        if (name.contains("stormcaller") && (raceId.contains("thunder") || raceId.contains("lightning") || raceId.contains("storm"))) {
            e.setDamage(e.getDamage() * 1.25);
            p.getWorld().strikeLightningEffect(e.getEntity().getLocation());
        }
    }
}
