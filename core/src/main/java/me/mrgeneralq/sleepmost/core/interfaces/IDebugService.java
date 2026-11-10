package me.mrgeneralq.sleepmost.core.interfaces;

import org.bukkit.entity.Player;

public interface IDebugService {
    void enableFor(Player player);
    void disableFor(Player player);
    boolean isEnabledFor(Player player);

    /**
     * @return {@code true} when at least one recipient (console or a player) is
     * currently subscribed to debug output. Callers can use this to skip
     * building expensive diagnostic strings.
     */
    boolean isEnabled();

    void print(String logMsg);
}
