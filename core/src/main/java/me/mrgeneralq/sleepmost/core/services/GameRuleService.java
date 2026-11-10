package me.mrgeneralq.sleepmost.core.services;

import me.mrgeneralq.sleepmost.core.interfaces.IGameRuleService;
import org.bukkit.GameRule;
import org.bukkit.World;

import java.lang.reflect.Method;

/**
 * Sets the {@code advanceTime} game rule across Minecraft versions.
 * <p>
 * Older Paper/Spigot versions expose it as {@code GameRule.ADVANCE_TIME},
 * while newer Paper versions moved it to {@code GameRules.ADVANCE_TIME}.
 * Both are resolved reflectively so a single jar can run on either.
 */
public class GameRuleService implements IGameRuleService {

    private static final Method SET_GAME_RULE = resolveSetGameRule();
    private static final Object ADVANCE_TIME_RULE = resolveAdvanceTimeRule();

    @Override
    public void setAdvanceTime(World world, boolean value) {
        if (world == null || SET_GAME_RULE == null || ADVANCE_TIME_RULE == null) {
            return;
        }

        try {
            SET_GAME_RULE.invoke(world, ADVANCE_TIME_RULE, value);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            // The rule is unavailable or the world implementation rejected it.
        }
    }

    private static Method resolveSetGameRule() {
        try {
            return World.class.getMethod("setGameRule", GameRule.class, Object.class);
        } catch (NoSuchMethodException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static Object resolveAdvanceTimeRule() {
        for (String className : new String[]{"org.bukkit.GameRules", "org.bukkit.GameRule"}) {
            try {
                return Class.forName(className).getField("ADVANCE_TIME").get(null);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                // Try the next location.
            }
        }
        return null;
    }
}
