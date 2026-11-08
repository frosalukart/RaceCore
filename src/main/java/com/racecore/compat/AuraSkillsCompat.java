package com.racecore.compat;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.StatModifier;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AuraSkillsCompat {
    private static final String MODIFIER_NAME = "RaceCore_HealthBonus";

    /**
     * Memberikan bonus health ke pemain secara aman.
     * Kompatibel dengan AuraSkills maupun plugin RPG lainnya.
     */
    public static void applyRaceHealth(Player player, double bonusHealth) {
        removeRaceHealth(player); // Bersihkan sisa modifier lama

        if (bonusHealth <= 0) return;

        // Jika AuraSkills ada, sinkronisasi dengan sistem stat mereka
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
            // Fallback aman untuk plugin lain: gunakan AttributeModifier Spigot bawaan
            // Ini mencegah overwrite max health secara paksa (setBaseValue / setMaxHealth)
            AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                attr.addModifier(new AttributeModifier(
                        UUID.randomUUID(),
                        MODIFIER_NAME,
                        bonusHealth,
                        AttributeModifier.Operation.ADD_NUMBER
                ));
            }
        }
    }

    /**
     * Menghapus bonus health (digunakan saat ganti ras atau reset).
     */
    public static void removeRaceHealth(Player player) {
        if (Bukkit.getPluginManager().getPlugin("AuraSkills") != null) {
            try {
                AuraSkillsApi.get().getUser(player.getUniqueId()).removeStatModifier(MODIFIER_NAME);
            } catch (Exception e) {
                // Abaikan jika user tidak ditemukan/belum load API-nya
            }
        } else {
            AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                for (AttributeModifier mod : attr.getModifiers()) {
                    if (mod.getName().equals(MODIFIER_NAME)) {
                        attr.removeModifier(mod);
                    }
                }
            }
        }
    }
}
