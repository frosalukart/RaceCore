package com.racecore.compat;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.StatModifier;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

public class AuraSkillsCompat {
    private static final String MODIFIER_NAME = "RaceCore_HealthBonus";
    private static NamespacedKey key;

    private static NamespacedKey getKey() {
        if (key == null) {
            key = new NamespacedKey("racecore", "health_bonus");
        }
        return key;
    }

    public static void applyRaceHealth(Player player, double bonusHealth) {
        removeRaceHealth(player);

        if (bonusHealth <= 0) return;

        if (Bukkit.getPluginManager().getPlugin("AuraSkills") != null) {
            try {
                AuraSkillsApi api = AuraSkillsApi.get();
                api.getUser(player.getUniqueId()).addStatModifier(
                        new StatModifier(MODIFIER_NAME, api.getGlobalRegistry().getStat("health"), bonusHealth)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
            if (attr != null) {
                attr.addModifier(new AttributeModifier(
                        getKey(),
                        bonusHealth,
                        AttributeModifier.Operation.ADD_NUMBER
                ));
            }
        }
    }

    public static void removeRaceHealth(Player player) {
        if (Bukkit.getPluginManager().getPlugin("AuraSkills") != null) {
            try {
                AuraSkillsApi.get().getUser(player.getUniqueId()).removeStatModifier(MODIFIER_NAME);
            } catch (Exception e) {
            }
        } else {
            AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
            if (attr != null) {
                attr.removeModifier(getKey());
            }
        }
    }
}
