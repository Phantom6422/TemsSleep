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
     * Registers the main command. Paper plugins cannot use {@code plugin.yml}
     * commands, so the Paper/Purpur implementation registers through the server
     * command map.
     */
    default void registerCommand(Plugin plugin, String name, List<String> aliases,
                                 CommandExecutor executor, TabCompleter completer) {
    }
}
