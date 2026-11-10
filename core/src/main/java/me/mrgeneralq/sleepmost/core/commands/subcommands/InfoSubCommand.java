package me.mrgeneralq.sleepmost.core.commands.subcommands;

import me.mrgeneralq.sleepmost.core.enums.MessageKey;
import me.mrgeneralq.sleepmost.core.flags.ISleepFlag;
import me.mrgeneralq.sleepmost.core.interfaces.*;
import me.mrgeneralq.sleepmost.core.statics.CommandSenderUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static me.mrgeneralq.sleepmost.core.statics.ChatColorUtils.colorize;

public class InfoSubCommand implements ISubCommand {

    private final ISleepService sleepService;
    private final IMessageService messageService;
    private final IFlagService flagService;
    private final IFlagsRepository flagsRepository;

    public InfoSubCommand(ISleepService sleepService, IMessageService messageService, IFlagService flagService, IFlagsRepository flagsRepository) {
        this.sleepService = sleepService;
        this.messageService = messageService;
        this.flagService = flagService;
        this.flagsRepository = flagsRepository;
    }

    @Override
    public boolean executeCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if(!CommandSenderUtils.hasWorld(sender)){
            this.messageService.sendMessage(sender, messageService.getMessagePrefixed(MessageKey.NO_CONSOLE_COMMAND).build());
            return true;
        }

        World world = CommandSenderUtils.getWorldOf(sender);

        if(!sleepService.isEnabledAt(world))
        {
            this.messageService.sendMessage(sender, messageService.getMessagePrefixed(MessageKey.CURRENTLY_DISABLED)
                    .setWorld(world)
                    .build());
            return true;
        }
        sender.sendMessage(colorize("&b*********************************************"));
        sender.sendMessage(colorize(String.format("&e&lFLAGS &o&7world: &c&l%s", world.getName())));
        sender.sendMessage(colorize("&b*********************************************"));

        Set<ISleepFlag<?>> flagList = this.flagsRepository.getFlags();

        int entriesPerPage = 10;
        int page = 1;
        int totalPages = Math.max((int) Math.ceil(flagList.size() / (double) entriesPerPage), 1);

        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
                page = 1;
            }
        }

        // Stream.skip() throws for negative values and a page of 0 would skip
        // backwards. Keep the page inside the valid range.
        page = Math.max(1, Math.min(page, totalPages));

        flagList.stream()
                .sorted(comparing(ISleepFlag::getName))
                .map(flag -> getValueAtMessage(flag, world))
                .skip( entriesPerPage * (page - 1))
                .limit(entriesPerPage)
                .forEach(sender::sendMessage);
        sender.sendMessage("");
        sender.sendMessage(colorize(String.format("&b(&c%s&f/&c%s&b)  &7/ts info <page>", page , totalPages)));
        sender.sendMessage(colorize("&b*********************************************"));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, List<String> args) {
        if (args.size() != 1) {
            return new ArrayList<>();
        }

        int totalPages = Math.max((int) Math.ceil(this.flagsRepository.getFlags().size() / 10.0), 1);
        List<String> pages = new ArrayList<>(totalPages);
        for (int page = 1; page <= totalPages; page++) {
            pages.add(String.valueOf(page));
        }

        String prefix = args.get(0);
        return pages.stream().filter(page -> page.startsWith(prefix)).collect(Collectors.toList());
    }

    private String getValueAtMessage(ISleepFlag<?> flag, World world)
    {
        return messageService.getMessage("&e%flag% &b%value%")
                .setFlag(flag.getName())
                .setPlaceHolder("%value%", this.flagService.getValueDisplayName(flag, flag.getValueAt(world)))
                .build();
    }
}
