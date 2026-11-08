package com.racecore;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class RaceExpansion extends PlaceholderExpansion {
    private final RaceCore plugin;

    public RaceExpansion(RaceCore plugin) { this.plugin = plugin; }

    @Override public String getIdentifier() { return "race"; }
    @Override public String getAuthor() { return "RaceCore"; }
    @Override public String getVersion() { return plugin.getPluginMeta().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";
        Race r = plugin.data().raceOf(player.getUniqueId());
        switch (params.toLowerCase()) {
            case "name": return RaceCore.color(r.display);
            case "id": return r.id;
            case "rarity": return r.rarity;
            case "rerolls": return String.valueOf(plugin.data().get(player.getUniqueId()).rerollBalance);
            default: return null;
        }
    }
}
