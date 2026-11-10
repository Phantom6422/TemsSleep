package me.mrgeneralq.sleepmost.core.interfaces;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.List;

public interface IPlatformModule {

    /**
     * Called early during plugin enable, before the services are bootstrapped.
     */
    void setup();

    /**
     * Registers the main command when the platform does not provide one from
     * {@code plugin.yml} (Paper plugins register commands programmatically).
     *
     * <p>The default implementation is a no-op for platforms that already
     * registered the command from {@code plugin.yml}.</p>
     */
    default void registerCommand(Plugin plugin, String name, List<String> aliases,
                                 CommandExecutor executor, TabCompleter completer) {
    }
}
