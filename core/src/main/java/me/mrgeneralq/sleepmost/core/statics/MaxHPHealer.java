package me.mrgeneralq.sleepmost.core.statics;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public interface MaxHPHealer {
    void heal(Player player);

    MaxHPHealer
            LEGACY_HEALER = player -> player.setHealth(20),
            UPDATED_HEALER = player -> {
                AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null) {
                    // Use the effective value so health modifiers (e.g. from other plugins) are respected.
                    player.setHealth(maxHealth.getValue());
                }
            };
}
