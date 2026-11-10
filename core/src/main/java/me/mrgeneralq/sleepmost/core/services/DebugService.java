package me.mrgeneralq.sleepmost.core.services;

import me.mrgeneralq.sleepmost.core.interfaces.IConfigService;
import me.mrgeneralq.sleepmost.core.interfaces.IDebugService;
import me.mrgeneralq.sleepmost.core.interfaces.ISleepMostPlayerService;
import me.mrgeneralq.sleepmost.core.models.SleepMostPlayer;
import me.mrgeneralq.sleepmost.core.statics.ChatColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DebugService implements IDebugService {

    private final ISleepMostPlayerService sleepMostPlayerService;
    private final IConfigService configService;

    public DebugService(ISleepMostPlayerService sleepMostPlayerService, IConfigService configService) {
        this.sleepMostPlayerService = sleepMostPlayerService;
        this.configService = configService;
    }

    @Override
    public void enableFor(Player player) {
        this.sleepMostPlayerService.getPlayer(player).setDebugMode(true);
    }

    @Override
    public void disableFor(Player player) {
        this.sleepMostPlayerService.getPlayer(player).setDebugMode(false);
    }

    @Override
    public boolean isEnabledFor(Player player){
        return this.sleepMostPlayerService.getPlayer(player).hasDebugMode();
    }

    @Override
    public boolean isEnabled() {
        if (this.configService.debugModeEnabled()) {
            return true;
        }
        for (SleepMostPlayer player : this.sleepMostPlayerService.getAllPlayers()) {
            if (player.hasDebugMode()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void print(String logMsg) {

        if (logMsg == null || logMsg.isEmpty()) {
            return;
        }

        List<SleepMostPlayer> debuggingPlayers = new ArrayList<>();
        for (SleepMostPlayer player : this.sleepMostPlayerService.getAllPlayers()) {
            if (player.hasDebugMode() && player.getPlayerUUID().isOnline()) {
                debuggingPlayers.add(player);
            }
        }

        if (!debuggingPlayers.isEmpty()) {
            String colored = ChatColorUtils.colorize(logMsg);
            for (SleepMostPlayer player : debuggingPlayers) {
                Player onlinePlayer = player.getPlayerUUID().getPlayer();
                if (onlinePlayer != null) {
                    onlinePlayer.sendMessage(colored);
                }
            }
        }

        if(this.configService.debugModeEnabled())
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColorUtils.colorize(logMsg));
    }
}
