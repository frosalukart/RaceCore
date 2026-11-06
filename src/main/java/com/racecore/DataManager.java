package com.racecore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {
    private final RaceCore plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<UUID, PlayerData> players = new HashMap<>();

    public static class PlayerData {
        public String race = "human";
        public int rerollBalance = 0;
        public boolean freeRollUsed = false;
        public int pityCount = 0;
    }

    public DataManager(RaceCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
    }

    public void load() {
        if (!file.exists()) plugin.saveResource("players.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
        players.clear();

        if (config.isConfigurationSection("players")) {
            for (String key : config.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    PlayerData d = new PlayerData();
                    d.race = config.getString("players." + key + ".race", "human");
                    d.rerollBalance = config.getInt("players." + key + ".rerollBalance", 0);
                    d.freeRollUsed = config.getBoolean("players." + key + ".freeRollUsed", false);
                    d.pityCount = config.getInt("players." + key + ".pityCount", 0);
                    players.put(id, d);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void save() {
        if (config == null) return;
        for (Map.Entry<UUID, PlayerData> e : players.entrySet()) {
            String path = "players." + e.getKey().toString();
            config.set(path + ".race", e.getValue().race);
            config.set(path + ".rerollBalance", e.getValue().rerollBalance);
            config.set(path + ".freeRollUsed", e.getValue().freeRollUsed);
            config.set(path + ".pityCount", e.getValue().pityCount);
        }
        try { config.save(file); } catch (IOException ex) { plugin.getLogger().severe("Could not save players.yml"); }
    }

    public PlayerData get(UUID id) {
        return players.computeIfAbsent(id, k -> new PlayerData());
    }

    public Race raceOf(UUID id) {
        PlayerData d = get(id);
        Race r = plugin.races().get(d.race);
        return r != null ? r : plugin.races().human();
    }

    public int migrateOld() {
        File oldFile = new File(plugin.getDataFolder().getParentFile(), "RaceSystem/players.yml");
        if (!oldFile.exists()) return -1;
        YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
        if (!oldConfig.isConfigurationSection("players")) return 0;
        int count = 0;
        for (String key : oldConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                PlayerData d = get(id);
                d.race = oldConfig.getString("players." + key + ".race", d.race);
                d.rerollBalance = oldConfig.getInt("players." + key + ".rerollBalance", d.rerollBalance);
                d.freeRollUsed = oldConfig.getBoolean("players." + key + ".freeRollUsed", d.freeRollUsed);
                d.pityCount = oldConfig.getInt("players." + key + ".pityCount", d.pityCount);
                count++;
            } catch (IllegalArgumentException ignored) {}
        }
        save();
        return count;
    }
}
