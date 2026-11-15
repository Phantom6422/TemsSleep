package me.mrgeneralq.sleepmost.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class PlayerUtils {

    public static boolean isRealPlayer(Player player){
        return Bukkit.getPlayer(player.getUniqueId()) != null;
    }

    /**
     * Restores the player to their effective maximum health, including health
     * modifiers contributed by other plugins.
     */
    public static void healToMaxHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            player.setHealth(maxHealth.getValue());
        }
    }
}
