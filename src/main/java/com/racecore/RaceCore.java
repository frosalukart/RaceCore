package com.racecore;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RaceCore extends JavaPlugin {
    private RaceManager races;
    private DataManager data;
    private AbilityManager abilities;
    private Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!new File(getDataFolder(), "races/default.yml").exists()) saveResource("races/default.yml", false);

        races = new RaceManager(this);
        int count = races.load();
        data = new DataManager(this);
        boolean firstRun = !new File(getDataFolder(), "players.yml").exists();
        data.load();
        if (firstRun) {
            int migrated = data.migrateOld();
            if (migrated > 0) getLogger().info("Migrated " + migrated + " players from old RaceSystem.");
        }

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) economy = rsp.getProvider();
        }

        abilities = new AbilityManager(this);
        abilities.start();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new WeaponSynergyListener(this), this);

        Commands cmds = new Commands(this);
        for (String name : new String[]{"race", "reroll", "removerace", "racemanage", "racesystem"}) {
            PluginCommand pc = getCommand(name);
            if (pc != null) { pc.setExecutor(cmds); pc.setTabCompleter(cmds); }
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RaceExpansion(this).register();
        }
        getLogger().info("Loaded " + count + " races with Pity System & Synergy.");
    }

    @Override
    public void onDisable() {
        if (data != null) data.save();
        for (Player p : Bukkit.getOnlinePlayers()) abilities.release(p, null);
    }

    public RaceManager races() { return races; }
    public DataManager data() { return data; }
    public AbilityManager abilities() { return abilities; }
    public Economy economy() { return economy; }

    public static String color(String s) { return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }

    public String msg(String key, String... pairs) {
        String s = getConfig().getString("messages." + key, key);
        for (int i = 0; i + 1 < pairs.length; i += 2) s = s.replace(pairs[i], pairs[i + 1]);
        return color(getConfig().getString("messages.prefix", "") + s);
    }

    public void reloadAll() {
        reloadConfig();
        races.load();
    }

    public void setRace(org.bukkit.OfflinePlayer target, Race race) {
        DataManager.PlayerData d = data.get(target.getUniqueId());
        Race old = races.get(d.race);
        d.race = race.id;
        data.save();
        Player on = target.getPlayer();
        if (on != null) {
            abilities.release(on, old);
            abilities.apply(on);
        }
    }

    public void rerollNow(Player p) {
        DataManager.PlayerData d = data.get(p.getUniqueId());
        if (d.rerollBalance < 1) { p.sendMessage(msg("no-rerolls")); return; }
        d.rerollBalance--;
        d.pityCount++;

        Race r = races.rollWithPity(d.pityCount);

        String rarity = r.rarity.toLowerCase();
        if (rarity.contains("epic") || rarity.contains("legendary") || rarity.contains("mythic")) {
            d.pityCount = 0;
        }

        setRace(p, r);
        data.save();
        p.sendMessage(msg("roll-success", "{race}", color(r.display)));
    }

    public void buyReroll(Player p) {
        if (economy == null) { p.sendMessage(msg("no-economy")); return; }
        double cost = getConfig().getDouble("economy.reroll-cost", 5000000);
        String c = String.format("%,.0f", cost);
        if (!economy.has(p, cost) || !economy.withdrawPlayer(p, cost).transactionSuccess()) {
            p.sendMessage(msg("not-enough-money", "{cost}", c));
            return;
        }
        DataManager.PlayerData d = data.get(p.getUniqueId());
        d.rerollBalance++;
        data.save();
        p.sendMessage(msg("reroll-bought", "{cost}", c, "{balance}", String.valueOf(d.rerollBalance)));
    }
}
