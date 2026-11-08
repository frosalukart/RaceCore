package com.racecore;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffect;

import java.util.Map;
import java.util.Set;

public class CombatListener implements Listener {
    private static final Set<String> UNDEAD = Set.of("ZOMBIE", "SKELETON", "STRAY", "HUSK", "DROWNED",
            "ZOMBIE_VILLAGER", "WITHER_SKELETON", "PHANTOM", "ZOMBIFIED_PIGLIN", "BOGGED", "PARCHED");
    private final RaceCore plugin;

    public CombatListener(RaceCore plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        Player attacker = null;
        Entity d = e.getDamager();
        if (d instanceof Player pl) attacker = pl;
        else if (d instanceof Projectile pr && pr.getShooter() instanceof Player pl) attacker = pl;

        double dmg = e.getDamage();
        if (attacker != null) {
            Race r = plugin.data().raceOf(attacker.getUniqueId());
            dmg *= r.attackMultiplier;
            if (e.getEntity() instanceof LivingEntity target) {
                for (Map.Entry<org.bukkit.potion.PotionEffectType, int[]> en : r.onHit.entrySet()) {
                    target.addPotionEffect(new PotionEffect(en.getKey(), en.getValue()[0], en.getValue()[1]));
                }
            }
            if (r.lifesteal > 0) {
                AttributeInstance max = attacker.getAttribute(Attribute.MAX_HEALTH);
                double cap = max == null ? 20 : max.getValue();
                attacker.setHealth(Math.min(cap, attacker.getHealth() + dmg * r.lifesteal));
            }
        }
        if (e.getEntity() instanceof Player victim) {
            dmg *= plugin.data().raceOf(victim.getUniqueId()).damageTakenMultiplier;
        }
        e.setDamage(dmg);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFall(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && e.getEntity() instanceof Player p
                && plugin.data().raceOf(p.getUniqueId()).fallImmune) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player p && UNDEAD.contains(e.getEntityType().name())
                && plugin.data().raceOf(p.getUniqueId()).mobsIgnoreUndead) {
            e.setCancelled(true);
        }
    }
}
