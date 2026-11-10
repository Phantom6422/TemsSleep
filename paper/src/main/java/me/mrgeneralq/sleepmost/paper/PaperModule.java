package me.mrgeneralq.sleepmost.paper;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.mrgeneralq.sleepmost.core.interfaces.IPlatformModule;
import me.mrgeneralq.sleepmost.core.providers.GameRuleProvider;
import me.mrgeneralq.sleepmost.paper.services.PaperGameRuleService;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PaperModule implements IPlatformModule {

    @Override
    public void setup(){
        GameRuleProvider.registerGameRuleService(new PaperGameRuleService());
    }

    @Override
    public void registerCommand(Plugin plugin, String name, List<String> aliases,
                                CommandExecutor executor, TabCompleter completer) {

        if (!(plugin instanceof JavaPlugin javaPlugin)) {
            return;
        }

        // Paper plugins reject JavaPlugin#getCommand and register commands through
        // this lifecycle-backed API instead.
        javaPlugin.registerCommand(name, "open the TemsSleep help menu", aliases, new BasicCommand() {
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
    }
}
