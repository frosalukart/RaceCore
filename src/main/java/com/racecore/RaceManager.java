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
        List<Race> pool = new ArrayList<>();
        int total = 0;
        for (Race r : races.values()) {
            if (!r.rollable) continue;
            pool.add(r);
            total += Math.max(1, plugin.getConfig().getInt("roll-weights." + r.rarity, 10));
        }
        if (pool.isEmpty()) return human();
        int pick = ThreadLocalRandom.current().nextInt(total);
        for (Race r : pool) {
            pick -= Math.max(1, plugin.getConfig().getInt("roll-weights." + r.rarity, 10));
            if (pick < 0) return r;
        }
        return pool.get(0);
    }
}
