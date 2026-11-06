package com.racecore;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AbilityManager {
    private final RaceCore plugin;
    private final NamespacedKey speedKey;
    private final Set<UUID> flyers = new HashSet<>();

    public AbilityManager(RaceCore plugin) {
        this.plugin = plugin;
        this.speedKey = new NamespacedKey(plugin, "race_speed");
    }

    public void start() {
        new BukkitRunnable() {
            @Override public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) apply(p);
            }
        }.runTaskTimer(plugin, 20L, 40L);
    }

    private boolean isNight(World w) {
        if (w.getEnvironment() != World.Environment.NORMAL) return false;
        long t = w.getTime();
        return t >= 13000 && t < 23000;
    }

    private void setBase(Player p, Attribute a, double v) {
        AttributeInstance inst = p.getAttribute(a);
        if (inst != null && inst.getBaseValue() != v) inst.setBaseValue(v);
    }

    private void give(Player p, Map<PotionEffectType, Integer> m) {
        if (m == null) return;
        for (Map.Entry<PotionEffectType, Integer> e : m.entrySet()) {
            int dur = e.getKey().equals(PotionEffectType.NIGHT_VISION) ? 260 : 100;
            p.addPotionEffect(new PotionEffect(e.getKey(), dur, Math.max(0, e.getValue()), true, false, false));
        }
    }

    public void apply(Player p) {
        Race r = plugin.data().raceOf(p.getUniqueId());
        World w = p.getWorld();
        boolean night = isNight(w);
        boolean day = w.getEnvironment() == World.Environment.NORMAL && !night;
        boolean nether = w.getEnvironment() == World.Environment.NETHER;

        double hp = (night && r.nightMaxHealth > 0) ? r.nightMaxHealth : r.maxHealth;
        hp = Math.max(2.0, hp);
        setBase(p, Attribute.MAX_HEALTH, hp);
        setBase(p, Attribute.SCALE, r.scale);
        setBase(p, Attribute.KNOCKBACK_RESISTANCE, r.knockbackResistance);
        if (p.getHealth() > hp) p.setHealth(hp);

        AttributeInstance speed = p.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(speedKey);
            if (r.speedMultiplier != 0) {
                speed.addModifier(new AttributeModifier(speedKey, r.speedMultiplier, AttributeModifier.Operation.ADD_SCALAR));
            }
        }

        GameMode gm = p.getGameMode();
        boolean survival = gm == GameMode.SURVIVAL || gm == GameMode.ADVENTURE;

        give(p, r.effects.get("effects"));
        if (day) give(p, r.effects.get("day-effects"));
        if (night) give(p, r.effects.get("night-effects"));
        if (nether) give(p, r.effects.get("nether-effects"));
        if (p.isUnderWater()) give(p, r.effects.get("water-effects"));
        Block b = p.getLocation().getBlock();
        if (b.getLightFromSky() == 0 && p.getLocation().getBlockY() < 60) give(p, r.effects.get("underground-effects"));
        if (b.getTemperature() < 0.15) give(p, r.effects.get("cold-effects"));

        if (survival) {
            if (r.noHunger) { p.setFoodLevel(20); }
            if (r.burnsInSun && day && !w.hasStorm() && b.getLightFromSky() == 15
                    && !p.isInWater() && p.getInventory().getHelmet() == null) {
                p.setFireTicks(r.sunFireTicks);
            }
            boolean wantFly = r.fly || (nether && r.netherFly);
            UUID id = p.getUniqueId();
            if (wantFly) {
                if (!p.getAllowFlight()) { p.setAllowFlight(true); flyers.add(id); }
            } else if (flyers.remove(id)) {
                p.setAllowFlight(false);
                p.setFlying(false);
            }
        }
    }

    /** Removes everything the given race granted (call when the race changes or player leaves). */
    public void release(Player p, Race old) {
        if (old != null) for (PotionEffectType t : old.allEffectTypes()) p.removePotionEffect(t);
        if (flyers.remove(p.getUniqueId()) && (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)) {
            p.setAllowFlight(false);
            p.setFlying(false);
        }
    }
}
