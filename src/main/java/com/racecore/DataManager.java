package com.racecore;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    public static class PlayerData {
        public String race = "human";
        public boolean freeRollUsed = false;
        public int rerollBalance = 0;
    }

    private final RaceCore plugin;
    private final File file;
    private final Map<UUID, PlayerData> map = new HashMap<>();

    public DataManager(RaceCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
    }

    public void load() {
        map.clear();
        YamlConfiguration c = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection s = c.getConfigurationSection("players");
        if (s == null) return;
        for (String key : s.getKeys(false)) {
            try {
                PlayerData d = new PlayerData();
                d.race = s.getString(key + ".race", "human").toLowerCase();
                d.freeRollUsed = s.getBoolean(key + ".free-roll-used", false);
                d.rerollBalance = s.getInt(key + ".reroll-balance", 0);
                map.put(UUID.fromString(key), d);
            } catch (IllegalArgumentException ignored) { }
        }
    }

    public synchronized void save() {
        YamlConfiguration c = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerData> e : map.entrySet()) {
            String k = "players." + e.getKey();
            c.set(k + ".race", e.getValue().race);
            c.set(k + ".free-roll-used", e.getValue().freeRollUsed);
            c.set(k + ".reroll-balance", e.getValue().rerollBalance);
        }
        try { c.save(file); } catch (IOException ex) { plugin.getLogger().severe("Could not save players.yml: " + ex.getMessage()); }
    }

    public PlayerData get(UUID id) { return map.computeIfAbsent(id, k -> new PlayerData()); }
    public boolean has(UUID id) { return map.containsKey(id); }

    public Race raceOf(UUID id) {
        Race r = plugin.races().get(get(id).race);
        return r == null ? plugin.races().human() : r;
    }

    public int migrateOld() {
        File old = new File(plugin.getDataFolder().getParentFile(), "RaceSystem/playerdata.yml");
        if (!old.exists()) return -1;
        YamlConfiguration c = YamlConfiguration.loadConfiguration(old);
        ConfigurationSection s = c.getConfigurationSection("players");
        if (s == null) return 0;
        int count = 0;
        for (String key : s.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                PlayerData d = new PlayerData();
                String race = s.getString(key + ".race", "human").toLowerCase();
                d.race = plugin.races().get(race) != null ? race : "human";
                d.freeRollUsed = s.getBoolean(key + ".free-roll-used", true);
                d.rerollBalance = s.getInt(key + ".reroll-balance", 0);
                map.put(id, d);
                count++;
            } catch (IllegalArgumentException ignored) { }
        }
        save();
        return count;
    }
}
