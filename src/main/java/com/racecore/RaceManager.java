package com.racecore;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class RaceManager {
    private final RaceCore plugin;
    private final Map<String, Race> races = new LinkedHashMap<>();

    public RaceManager(RaceCore plugin) { this.plugin = plugin; }

    public int load() {
        races.clear();
        File folder = new File(plugin.getDataFolder(), "races");
        if (!folder.exists()) folder.mkdirs();

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File f : files) {
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
                for (String key : yml.getKeys(false)) {
                    if (yml.isConfigurationSection(key)) {
                        ConfigurationSection sec = yml.getConfigurationSection(key);
                        Race r = parseRace(key, sec);
                        if (r != null) races.put(r.id.toLowerCase(Locale.ROOT), r);
                    }
                }
            }
        }
        return races.size();
    }

    private Race parseRace(String id, ConfigurationSection sec) {
        String display = sec.getString("display", id);
        String rarity = sec.getString("rarity", "Common");
        double chance = sec.getDouble("chance", 10.0);
        boolean rollable = sec.getBoolean("rollable", true);
        Material icon = Material.matchMaterial(sec.getString("icon", "PAPER"));
        if (icon == null) icon = Material.PAPER;
        List<String> desc = sec.getStringList("description");

        Race r = new Race(id, display, rarity, chance, rollable, icon, desc);

        if (sec.isConfigurationSection("attributes")) {
            for (String attr : sec.getConfigurationSection("attributes").getKeys(false)) {
                r.attributes.put(attr.toUpperCase(Locale.ROOT), sec.getDouble("attributes." + attr));
            }
        }
        if (sec.isConfigurationSection("effects")) {
            for (String eff : sec.getConfigurationSection("effects").getKeys(false)) {
                r.effects.put(eff.toUpperCase(Locale.ROOT), sec.getInt("effects." + eff));
            }
        }
        return r;
    }

    public Collection<Race> all() { return races.values(); }
    public Race get(String id) { return id == null ? null : races.get(id.toLowerCase(Locale.ROOT)); }

    public Race human() {
        Race h = races.get("human");
        return h != null ? h : new Race("human", "&fHuman", "Common", 100.0, true, Material.PLAYER_HEAD, Collections.singletonList("&7Manusia biasa tanpa efek bonus."));
    }

    public List<String> ids() { return new ArrayList<>(races.keySet()); }

    /** Rolling system with Dynamic Luck / Pity */
    public Race rollWithPity(int pityCount) {
        List<Race> candidates = new ArrayList<>();
        double totalWeight = 0;

        for (Race r : races.values()) {
            if (!r.rollable) continue;

            double weight = r.chance;
            String rarity = r.rarity.toLowerCase(Locale.ROOT);

            // Dynamic Luck / Pity Weight Multiplier based on pityCount
            if (pityCount >= 10) {
                if (rarity.contains("mythic")) weight *= (1.0 + (pityCount - 9) * 0.5);
                else if (rarity.contains("legendary")) weight *= (1.0 + (pityCount - 9) * 0.4);
                else if (rarity.contains("epic")) weight *= (1.0 + (pityCount - 9) * 0.25);
                else if (rarity.contains("common")) weight *= Math.max(0.1, 1.0 - (pityCount - 9) * 0.1);
            }

            totalWeight += weight;
            candidates.add(r);
        }

        if (candidates.isEmpty()) return human();

        // Hard Pity guarantee at 15 rerolls
        if (pityCount >= 15) {
            List<Race> highTier = new ArrayList<>();
            for (Race r : candidates) {
                String rarity = r.rarity.toLowerCase(Locale.ROOT);
                if (rarity.contains("epic") || rarity.contains("legendary") || rarity.contains("mythic")) {
                    highTier.add(r);
                }
            }
            if (!highTier.isEmpty()) return highTier.get(new Random().nextInt(highTier.size()));
        }

        double rng = new Random().nextDouble() * totalWeight;
        double current = 0;
        for (Race r : candidates) {
            double weight = r.chance;
            String rarity = r.rarity.toLowerCase(Locale.ROOT);
            if (pityCount >= 10) {
                if (rarity.contains("mythic")) weight *= (1.0 + (pityCount - 9) * 0.5);
                else if (rarity.contains("legendary")) weight *= (1.0 + (pityCount - 9) * 0.4);
                else if (rarity.contains("epic")) weight *= (1.0 + (pityCount - 9) * 0.25);
                else if (rarity.contains("common")) weight *= Math.max(0.1, 1.0 - (pityCount - 9) * 0.1);
            }
            current += weight;
            if (rng <= current) return r;
        }

        return candidates.get(0);
    }
}
