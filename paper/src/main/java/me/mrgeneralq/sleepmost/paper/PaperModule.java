package me.mrgeneralq.sleepmost.paper;

import me.mrgeneralq.sleepmost.core.interfaces.IPlatformModule;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Paper platform support.
 * <p>
 * Paper plugins reject {@code JavaPlugin#getCommand}, so the command is
 * registered through the command map. The command map API is available on
 * Paper 1.21.x as well as 26.x, which keeps the same jar usable on both.
 */
public class PaperModule implements IPlatformModule {

    @Override
    public void setup() {
        // Paper needs no platform-specific bootstrap.
    }

    @Override
    public void registerCommand(Plugin plugin, String name, List<String> aliases,
                                CommandExecutor executor, TabCompleter completer) {

        if (!isPaper()) {
            return;
        }

        CommandMap commandMap = Bukkit.getServer().getCommandMap();
        if (commandMap.getCommand(name) != null) {
            return;
        }

        Command command = new Command(name, "open the TemsSleep help menu", "/" + name, aliases) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
                return executor.onCommand(sender, this, commandLabel, args);
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
                List<String> completions = completer.onTabComplete(sender, this, alias, args);
                return completions == null ? Collections.emptyList() : completions;
            }
        };

        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), command);
    }

    private static boolean isPaper() {
        try {
            Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
