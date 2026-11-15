package me.mrgeneralq.sleepmost.core.commands.subcommands;

import me.mrgeneralq.sleepmost.core.builders.MessageBuilder;
import me.mrgeneralq.sleepmost.core.enums.MessageKey;
import me.mrgeneralq.sleepmost.core.interfaces.IFlagsRepository;
import me.mrgeneralq.sleepmost.core.interfaces.IMessageService;
import me.mrgeneralq.sleepmost.core.interfaces.ISleepService;
import me.mrgeneralq.sleepmost.core.interfaces.ISubCommand;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class KickSubCommand implements ISubCommand {

    private final ISleepService sleepService;
    private final IMessageService messageService;
    private final IFlagsRepository flagsRepository;

    public KickSubCommand(ISleepService sleepService, IMessageService messageService, IFlagsRepository flagsRepository) {
    this.sleepService = sleepService;
    this.messageService = messageService;
        this.flagsRepository = flagsRepository;
    }

    @Override
    public boolean executeCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if(!(sender instanceof Player)){
            this.messageService.sendMessage(sender, messageService.getMessagePrefixed(MessageKey.NO_CONSOLE_COMMAND).build());
            return true;
        }

        Player player = (Player) sender;

        if(args.length < 2){
            this.messageService.sendMessage(sender, messageService.getMessagePrefixed(MessageKey.SPECIFY_PLAYER).build());
            return true;
        }

        String targetPlayerName = args[1];

        Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);

        if(targetPlayer == null){
            this.messageService.sendMessage(sender, messageService.getMessagePrefixed(MessageKey.TARGET_NOT_ONLINE)
                    .setPlayer(player)
                    .build());
            return true;
        }

        // Kicking is controlled per world; read the target's world, not the
        // sender's, so a player in a kick-enabled world cannot kick a sleeper
        // in a world where kicking is disabled.
        World targetWorld = targetPlayer.getWorld();
        if(!this.flagsRepository.getAllowKickFlag().getValueAt(targetWorld)){
            MessageBuilder kickNotAllowedMsg = this.messageService.getMessagePrefixed(MessageKey.KICKING_NOT_ALLOWED)
                    .setWorld(targetWorld);
            this.messageService.sendMessage(player, kickNotAllowedMsg.build());
            return true;
        }

        if(!this.sleepService.isPlayerAsleep(targetPlayer)){
            this.messageService.sendMessage(sender, messageService.getMessagePrefixed(MessageKey.TARGET_NOT_SLEEPING)
                    .setPlayer(targetPlayer)
                    .build());
            return true;
        }



        MessageBuilder targetKickedFromBedMsg = this.messageService.getMessagePrefixed(MessageKey.KICKED_PLAYER_FROM_BED)
                .setPlayer(targetPlayer)
                .setWorld(targetPlayer.getWorld());

        MessageBuilder youAreKickedMsg = this.messageService.getMessagePrefixed(MessageKey.YOU_ARE_KICKED_FROM_BED)
                .setPlayer(player);

        targetPlayer.teleport(targetPlayer.getLocation());
        this.messageService.sendMessage(sender,targetKickedFromBedMsg.setPlayer(targetPlayer).build());
        this.messageService.sendMessage(targetPlayer, youAreKickedMsg.setPlayer(player).build());
        this.sleepService.setSleeping(targetPlayer, false);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, List<String> args) {
        if (!(sender instanceof Player) || args.size() != 1) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        String prefix = args.get(0).toLowerCase(Locale.ROOT);

        return this.sleepService.getSleepers(player.getWorld()).stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(java.util.stream.Collectors.toList());
    }
}
