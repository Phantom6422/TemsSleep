package me.mrgeneralq.sleepmost.core.services;

import me.mrgeneralq.sleepmost.core.interfaces.IGameRuleService;
import org.bukkit.GameRules;
import org.bukkit.World;

/**
 * Sets the {@code advanceTime} game rule using the Paper 26.3 game-rule API.
 */
public class GameRuleService implements IGameRuleService {

    @Override
    public void setAdvanceTime(World world, boolean value) {
        if (world == null) {
            return;
        }
        world.setGameRule(GameRules.ADVANCE_TIME, value);
    }
}
