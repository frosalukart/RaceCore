package com.racecore;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Commands implements CommandExecutor, TabCompleter {
    private final RaceCore plugin;

    public Commands(RaceCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String label, String[] a) {
        switch (c.getName().toLowerCase(Locale.ROOT)) {
            case "race": return race(s);
            case "reroll": return reroll(s, a);
            case "removerace": return remove(s);
            case "racemanage": return manage(s, a);
            case "racesystem": return system(s, a);
            default: return false;
        }
    }

    private boolean race(CommandSender s) {
        if (!(s instanceof Player p)) { s.sendMessage(plugin.msg("player-only")); return true; }
        Race r = plugin.data().raceOf(p.getUniqueId());
        p.sendMessage(plugin.msg("your-race", "{race}", RaceCore.color(r.display)));
        RaceMenu.openMain(plugin, p);
        return true;
    }

    private boolean reroll(CommandSender s, String[] a) {
        if (!(s instanceof Player p)) { s.sendMessage(plugin.msg("player-only")); return true; }
        if (a.length > 0) {
            switch (a[0].toLowerCase(Locale.ROOT)) {
                case "balance":
                    p.sendMessage(plugin.msg("reroll-balance", "{balance}", String.valueOf(plugin.data().get(p.getUniqueId()).rerollBalance)));
                    return true;
                case "buy": plugin.buyReroll(p); return true;
                case "use": plugin.rerollNow(p); return true;
                default: break;
            }
        }
        RaceMenu.openMain(plugin, p);
        return true;
    }

    private boolean remove(CommandSender s) {
        if (!(s instanceof Player p)) { s.sendMessage(plugin.msg("player-only")); return true; }
        plugin.setRace(p, plugin.races().human());
        p.sendMessage(plugin.msg("removed"));
        return true;
    }

    private OfflinePlayer find(String name) {
        Player on = Bukkit.getPlayerExact(name);
        if (on != null) return on;
        return Bukkit.getOfflinePlayerIfCached(name);
    }

    private boolean manage(CommandSender s, String[] a) {
        if (!s.hasPermission("racecore.admin")) { s.sendMessage(plugin.msg("no-permission")); return true; }
        if (a.length == 0) { s.sendMessage(RaceCore.color("&cUsage: /racemanage <player> [set <race>|givereroll <n>|takereroll <n>|setreroll <n>]")); return true; }
        OfflinePlayer t = find(a[0]);
        if (t == null) { s.sendMessage(plugin.msg("player-not-found")); return true; }
        DataManager.PlayerData d = plugin.data().get(t.getUniqueId());
        String name = t.getName() == null ? a[0] : t.getName();
        if (a.length == 1) {
            Race r = plugin.data().raceOf(t.getUniqueId());
            s.sendMessage(plugin.msg("player-race", "{player}", name, "{race}", RaceCore.color(r.display), "{balance}", String.valueOf(d.rerollBalance)));
            return true;
        }
        String sub = a[1].toLowerCase(Locale.ROOT);
        if (sub.equals("set")) {
            Race r = a.length > 2 ? plugin.races().get(a[2]) : null;
            if (r == null) { s.sendMessage(plugin.msg("invalid-race", "{races}", String.join(", ", plugin.races().ids()))); return true; }
            plugin.setRace(t, r);
            s.sendMessage(plugin.msg("admin-set", "{player}", name, "{race}", RaceCore.color(r.display)));
            return true;
        }
        if (sub.equals("givereroll") || sub.equals("takereroll") || sub.equals("setreroll")) {
            int n;
            try { n = Integer.parseInt(a.length > 2 ? a[2] : "x"); } catch (NumberFormatException ex) { s.sendMessage(RaceCore.color("&cAmount must be a number.")); return true; }
            if (sub.equals("givereroll")) d.rerollBalance += n;
            else if (sub.equals("takereroll")) d.rerollBalance = Math.max(0, d.rerollBalance - n);
            else d.rerollBalance = Math.max(0, n);
            plugin.data().save();
            s.sendMessage(plugin.msg("admin-balance", "{player}", name, "{balance}", String.valueOf(d.rerollBalance)));
            return true;
        }
        s.sendMessage(RaceCore.color("&cUnknown subcommand."));
        return true;
    }

    private boolean system(CommandSender s, String[] a) {
        if (!s.hasPermission("racecore.admin")) { s.sendMessage(plugin.msg("no-permission")); return true; }
        String sub = a.length == 0 ? "help" : a[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload":
                plugin.reloadAll();
                for (Player p : Bukkit.getOnlinePlayers()) plugin.abilities().apply(p);
                s.sendMessage(plugin.msg("reloaded", "{count}", String.valueOf(plugin.races().all().size())));
                break;
            case "migrate":
                int n = plugin.data().migrateOld();
                s.sendMessage(n < 0 ? plugin.msg("migrate-none") : plugin.msg("migrated", "{count}", String.valueOf(n)));
                break;
            default:
                s.sendMessage(RaceCore.color("&6RaceCore &7- /race, /reroll [buy|balance], /removerace, /racemanage, /racesystem <reload|migrate>"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String label, String[] a) {
        List<String> out = new ArrayList<>();
        String name = c.getName().toLowerCase(Locale.ROOT);
        if (name.equals("reroll") && a.length == 1) { out.add("buy"); out.add("balance"); out.add("use"); }
        else if (name.equals("racesystem") && a.length == 1) { out.add("reload"); out.add("migrate"); out.add("help"); }
        else if (name.equals("racemanage")) {
            if (a.length == 1) for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
            else if (a.length == 2) { out.add("set"); out.add("givereroll"); out.add("takereroll"); out.add("setreroll"); }
            else if (a.length == 3 && a[1].equalsIgnoreCase("set")) out.addAll(plugin.races().ids());
        }
        String last = a.length == 0 ? "" : a[a.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(x -> !x.toLowerCase(Locale.ROOT).startsWith(last));
        return out;
    }
}
