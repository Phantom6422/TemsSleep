package me.mrgeneralq.sleepmost.core.commands.subcommands;

import me.mrgeneralq.sleepmost.core.enums.MessageKey;
import me.mrgeneralq.sleepmost.core.interfaces.IMessageService;
import me.mrgeneralq.sleepmost.core.interfaces.ISleepService;
import me.mrgeneralq.sleepmost.core.interfaces.ISubCommand;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BedSubCommand implements ISubCommand {

    private final ISleepService sleepService;
    private final IMessageService messageService;

    public BedSubCommand(ISleepService sleepService, IMessageService messageService) {
        this.sleepService = sleepService;
        this.messageService = messageService;
    }


    @Override
    public boolean executeCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if(!(sender instanceof Player)){
            this.messageService.sendMessage(sender, messageService.getMessagePrefixed(MessageKey.NO_CONSOLE_COMMAND).build());
            return true;
        }

        Player player = (Player) sender;
        World world = player.getWorld();


        if (!sleepService.isEnabledAt(world)) {
            this.messageService.sendMessage(sender, messageService.getMessagePrefixed(MessageKey.NOT_ENABLED_FOR_WORLD)
                    .setWorld(world)
                    .build());
            return true;
        }

        Location respawnLocation = player.getRespawnLocation();
        if (respawnLocation == null) {
            this.messageService.sendMessage(player, messageService.getMessagePrefixed(MessageKey.NO_BED_LOCATION_SET)
                    .setPlayer(player)
                    .setWorld(world)
                    .build());
            return true;
        }

        player.teleport(respawnLocation);
        this.messageService.sendMessage(player, messageService.getMessagePrefixed(MessageKey.TELEPORTED_TO_BED)
                .setPlayer(player)
                .setWorld(world)
                .build());
        return true;
    }
}

