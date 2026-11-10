package me.mrgeneralq.sleepmost.paper;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.mrgeneralq.sleepmost.core.interfaces.IPlatformModule;
import me.mrgeneralq.sleepmost.core.providers.GameRuleProvider;
import me.mrgeneralq.sleepmost.paper.services.PaperGameRuleService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PaperModule implements IPlatformModule {

    @Override
    public void setup(){
        try {
            Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
            GameRuleProvider.registerGameRuleService(new PaperGameRuleService());
        } catch (ClassNotFoundException ignored) {}
    }

    @Override
    public void registerCommand(Plugin plugin, String name, List<String> aliases,
                                CommandExecutor executor, TabCompleter completer) {

        // Recommended Paper API: JavaPlugin#getCommand is unsupported in Paper
        // plugins, but JavaPlugin#registerCommand is the supported replacement.
        if (plugin instanceof JavaPlugin javaPlugin) {
            javaPlugin.registerCommand(name, "open the sleepmost help menu", aliases, new BasicCommand() {
                @Override
                public void execute(@NotNull CommandSourceStack commandSourceStack, String[] args) {
                    executor.onCommand(commandSourceStack.getSender(), null, name, args);
                }

                @Override
                public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, String[] args) {
                    List<String> completions = completer.onTabComplete(commandSourceStack.getSender(), null, name, args);
                    return completions == null ? Collections.emptyList() : completions;
                }

                @Override
                public boolean canUse(@NotNull CommandSender sender) {
                    // SleepmostCommand performs the per-subcommand permission checks.
                    return true;
                }
            });
            return;
        }

        // Fallback for platforms exposing a Bukkit command map.
        CommandMap commandMap = Bukkit.getServer().getCommandMap();
        if (commandMap.getCommand(name) != null) {
            return;
        }

        Command command = new Command(name, "open the sleepmost help menu", "/" + name, aliases) {
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
}
