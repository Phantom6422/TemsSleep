package me.mrgeneralq.sleepmost.core.eventlisteners;

import me.mrgeneralq.sleepmost.core.interfaces.IBossBarService;
import me.mrgeneralq.sleepmost.core.interfaces.IMessageService;
import me.mrgeneralq.sleepmost.core.interfaces.ISleepMostPlayerService;
import me.mrgeneralq.sleepmost.core.interfaces.IUpdateService;
import me.mrgeneralq.sleepmost.core.statics.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import static me.mrgeneralq.sleepmost.core.statics.ChatColorUtils.colorize;

public class PlayerJoinEventListener implements Listener {

    private final IMessageService messageService;
    private final IBossBarService bossBarService;
    private final ISleepMostPlayerService sleepMostPlayerService;
    private final IUpdateService updateService;
    private final Plugin plugin;

    private static final String UPDATE_PERMISSION = "temssleep.alerts.update";
        private static final String LEGACY_UPDATE_PERMISSION = "sleepmost.alerts.update";

    public PlayerJoinEventListener(Plugin plugin, IUpdateService updateService, IMessageService messageService, IBossBarService bossBarService, ISleepMostPlayerService sleepMostPlayerService) {
        this.plugin = plugin;
        this.updateService = updateService;
        this.messageService = messageService;
        this.bossBarService = bossBarService;
        this.sleepMostPlayerService = sleepMostPlayerService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();

        // Profiles are removed on quit so the map stays bounded by online players.
        if(!this.sleepMostPlayerService.playerExists(player))
            this.sleepMostPlayerService.registerNewPlayer(player);


        if(ServerVersion.CURRENT_VERSION.supportsBossBars() && this.bossBarService != null)
            this.bossBarService.registerPlayer(player.getWorld(), player);

        if (!player.hasPermission(UPDATE_PERMISSION) && !player.hasPermission(LEGACY_UPDATE_PERMISSION))
            return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            if (!plugin.isEnabled()) {
                return;
            }
            if(updateService.hasUpdate()) {
                // Never touch the player from the async worker thread.
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        notifyNewUpdate(player);
                    }
                });
            }
        });
    }

    private void notifyNewUpdate(CommandSender sender) {
        sender.sendMessage(colorize("&b==============================================="));
        sender.sendMessage(messageService.getMessagePrefixed("&bA newer version of &esleep-most &bis available: &e%latest-version%")
                .setPlaceHolder("%latest-version%", updateService.getCachedUpdateVersion())
                .build());
        sender.sendMessage(ChatColor.GREEN + updateService.getUpdateUrl());
        sender.sendMessage(colorize("&eYou may ignore this message if you just updated"));
        sender.sendMessage(colorize("&b==============================================="));
    }
}
