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
 * Paper/Purpur platform support.
 * <p>
 * Paper plugins cannot use {@code JavaPlugin#getCommand}, so the command is
 * registered through the server command map. Purpur uses the same Paper API and
 * therefore the same registration path.
 */
public class PaperModule implements IPlatformModule {

    private Command registeredCommand;

    @Override
    public void setup() {
        // Paper and Purpur need no platform-specific bootstrap.
    }

    @Override
    public void registerCommand(Plugin plugin, String name, List<String> aliases,
                                CommandExecutor executor, TabCompleter completer) {

        CommandMap commandMap = Bukkit.getServer().getCommandMap();
        if (commandMap.getCommand(name) != null) {
            plugin.getLogger().warning("Command /" + name + " is already registered; skipping registration.");
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
        this.registeredCommand = command;
    }

    @Override
    public void unregisterCommand() {
        if (this.registeredCommand == null) {
            return;
        }
        this.registeredCommand.unregister(Bukkit.getServer().getCommandMap());
        this.registeredCommand = null;
    }
}
