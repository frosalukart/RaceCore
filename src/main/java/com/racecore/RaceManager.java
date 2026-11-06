package com.racecore;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RaceManager {
    private final RaceCore plugin;
    private final Map<String, Race> races = new LinkedHashMap<>();

    public RaceManager(RaceCore plugin) { this.plugin = plugin; }

    public int load() {
        races.clear();
        File dir = new File(plugin.getDataFolder(), "races");
        dir.mkdirs();
        File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".yml"));
        if (files != null) {
            Arrays.sort(files);
            for (File f : files) {
                YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
                for (String key : c.getKeys(false)) {
                    ConfigurationSection s = c.getConfigurationSection(key);
                    if (s == null) continue;
                    try {
                        races.put(key.toLowerCase(), Race.load(key.toLowerCase(), s));
                    } catch (Exception ex) {
                        plugin.getLogger().warning("Failed to load race '" + key + "' in " + f.getName() + ": " + ex.getMessage());
                    }
                }
            }
        }
        if (!races.containsKey("human")) races.put("human", new Race("human"));
        return races.size();
    }

    public Race get(String id) { return id == null ? null : races.get(id.toLowerCase()); }
    public Race human() { return races.get("human"); }
    public Collection<Race> all() { return races.values(); }
    public Set<String> ids() { return races.keySet(); }

    public Race roll() {
        return rollWithPity(0);
    }

    /** Roll system integrated with Pity / Dynamic Luck & Config Roll Weights */
    public Race rollWithPity(int pityCount) {
        List<Race> pool = new ArrayList<>();
        double totalWeight = 0;

        for (Race r : races.values()) {
            if (!r.rollable) continue;

            // Ambil bobot dasar dari config roll-weights, default 10
            double weight = Math.max(1, plugin.getConfig().getInt("roll-weights." + r.rarity, 10));
            String rarity = r.rarity.toUpperCase();

            // Pity System multiplier: Makin tinggi pityCount, makin besar kesempatan dapet rarity tinggi
            if (pityCount >= 10) {
                if (rarity.contains("MYTHIC")) weight *= (1.0 + (pityCount - 9) * 0.6);
                else if (rarity.contains("LEGENDARY")) weight *= (1.0 + (pityCount - 9) * 0.45);
                else if (rarity.contains("EPIC")) weight *= (1.0 + (pityCount - 9) * 0.3);
                else if (rarity.contains("COMMON")) weight *= Math.max(0.05, 1.0 - (pityCount - 9) * 0.15);
            }

            totalWeight += weight;
            pool.add(r);
        }

        if (pool.isEmpty()) return human();

        // Hard Pity guarantee di roll ke-15 (Dijamin dapet minimal Epic)
        if (pityCount >= 15) {
            List<Race> highTier = new ArrayList<>();
            for (Race r : pool) {
                String rarity = r.rarity.toUpperCase();
                if (rarity.contains("EPIC") || rarity.contains("LEGENDARY") || rarity.contains("MYTHIC")) {
                    highTier.add(r);
                }
            }
            if (!highTier.isEmpty()) {
                return highTier.get(ThreadLocalRandom.current().nextInt(highTier.size()));
            }
        }

        double pick = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double current = 0;
        
        for (Race r : pool) {
            double weight = Math.max(1, plugin.getConfig().getInt("roll-weights." + r.rarity, 10));
            String rarity = r.rarity.toUpperCase();
            if (pityCount >= 10) {
                if (rarity.contains("MYTHIC")) weight *= (1.0 + (pityCount - 9) * 0.6);
                else if (rarity.contains("LEGENDARY")) weight *= (1.0 + (pityCount - 9) * 0.45);
                else if (rarity.contains("EPIC")) weight *= (1.0 + (pityCount - 9) * 0.3);
                else if (rarity.contains("COMMON")) weight *= Math.max(0.05, 1.0 - (pityCount - 9) * 0.15);
            }
            current += weight;
            if (pick <= current) return r;
        }

        return pool.get(0);
    }
}
