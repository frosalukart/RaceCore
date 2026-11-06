package com.racecore;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/** A race loaded from a YAML section. Nothing here is hardcoded per race. */
public class Race {
    public static final String[] SECTIONS = {"effects", "day-effects", "night-effects", "nether-effects",
            "water-effects", "underground-effects", "cold-effects"};

    public final String id;
    public String display;
    public String rarity = "COMMON";
    public boolean rollable = true;
    public Material icon = Material.PAPER;
    public List<String> description = new ArrayList<>();

    public double maxHealth = 20, nightMaxHealth = -1, scale = 1, attackMultiplier = 1,
            damageTakenMultiplier = 1, speedMultiplier = 0, knockbackResistance = 0, lifesteal = 0;
    public boolean fly, netherFly, fallImmune, burnsInSun, noHunger, mobsIgnoreUndead;
    public int sunFireTicks = 100;

    public final Map<String, Map<PotionEffectType, Integer>> effects = new HashMap<>();
    /** effect -> {durationTicks, amplifier} applied to whatever this race hits */
    public final Map<PotionEffectType, int[]> onHit = new HashMap<>();

    public Race(String id) {
        this.id = id;
        this.display = "&f" + id;
    }

    public static Race load(String id, ConfigurationSection s) {
        Race r = new Race(id);
        r.display = s.getString("display", "&f" + id);
        r.rarity = s.getString("rarity", "COMMON").toUpperCase();
        r.rollable = s.getBoolean("rollable", true);
        Material m = Material.matchMaterial(s.getString("icon", "PAPER"));
        r.icon = m == null ? Material.PAPER : m;
        r.description = s.getStringList("description");

        r.maxHealth = s.getDouble("max-health", 20);
        r.nightMaxHealth = s.getDouble("night-max-health", -1);
        r.scale = s.getDouble("scale", 1);
        r.attackMultiplier = s.getDouble("attack-damage-multiplier", 1);
        r.damageTakenMultiplier = s.getDouble("damage-taken-multiplier", 1);
        r.speedMultiplier = s.getDouble("speed-multiplier", 0);
        r.knockbackResistance = s.getDouble("knockback-resistance", 0);
        r.lifesteal = s.getDouble("lifesteal", 0);
        r.fly = s.getBoolean("fly", false);
        r.netherFly = s.getBoolean("nether-fly", false);
        r.fallImmune = s.getBoolean("fall-immune", false);
        r.burnsInSun = s.getBoolean("burns-in-sun", false);
        r.noHunger = s.getBoolean("no-hunger", false);
        r.mobsIgnoreUndead = s.getBoolean("undead-mobs-ignore", false);
        r.sunFireTicks = s.getInt("sunlight-fire-ticks", 100);

        for (String sec : SECTIONS) {
            ConfigurationSection cs = s.getConfigurationSection(sec);
            Map<PotionEffectType, Integer> map = new HashMap<>();
            if (cs != null) {
                for (String key : cs.getKeys(false)) {
                    PotionEffectType t = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(key.toLowerCase()));
                    if (t == null) {
                        org.bukkit.Bukkit.getLogger().warning("[RaceCore] " + id + ": unknown effect '" + key + "'");
                        continue;
                    }
                    map.put(t, cs.getInt(key));
                }
            }
            r.effects.put(sec, map);
        }

        ConfigurationSection hit = s.getConfigurationSection("on-hit-effects");
        if (hit != null) {
            for (String key : hit.getKeys(false)) {
                PotionEffectType t = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(key.toLowerCase()));
                if (t == null) continue;
                String[] parts = hit.getString(key, "100:0").split(":");
                try {
                    r.onHit.put(t, new int[]{Integer.parseInt(parts[0]), parts.length > 1 ? Integer.parseInt(parts[1]) : 0});
                } catch (NumberFormatException ignored) { }
            }
        }
        return r;
    }

    public Set<PotionEffectType> allEffectTypes() {
        Set<PotionEffectType> all = new HashSet<>();
        for (Map<PotionEffectType, Integer> m : effects.values()) all.addAll(m.keySet());
        return all;
    }
}
