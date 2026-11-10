package me.mrgeneralq.sleepmost.core.services;

import me.mrgeneralq.sleepmost.core.interfaces.ISleepMostPlayerService;
import me.mrgeneralq.sleepmost.core.models.SleepMostPlayer;
import me.mrgeneralq.sleepmost.core.repositories.SleepMostPlayerRepository;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class SleepMostPlayerService implements ISleepMostPlayerService {

    SleepMostPlayerRepository repository;

    public SleepMostPlayerService(SleepMostPlayerRepository sleepMostPlayerRepository) {
        this.repository = sleepMostPlayerRepository;
    }

    @Override
    public SleepMostPlayer getPlayer(OfflinePlayer player) {
        SleepMostPlayer sleepMostPlayer = this.repository.get(player.getUniqueId());
        if (sleepMostPlayer == null && player.isOnline()) {
            // Lazy registration covers players that were online before the
            // plugin finished enabling, or a profile removed on quit.
            sleepMostPlayer = new SleepMostPlayer(player);
            this.repository.set(player.getUniqueId(), sleepMostPlayer);
        }
        return sleepMostPlayer;
    }

    @Override
    public void updatePlayer(SleepMostPlayer player) {
        this.repository.set(player.getPlayerUUID().getUniqueId(), player);
    }

    @Override
    public void unregisterPlayer(OfflinePlayer offlinePlayer) {
        this.repository.remove(offlinePlayer.getUniqueId());
    }

    @Override
    public void registerNewPlayer(OfflinePlayer offlinePlayer) {

        if(offlinePlayer == null || !offlinePlayer.isOnline()){
            //detect NPC
            return;
        }
        SleepMostPlayer player = new SleepMostPlayer(offlinePlayer);
        this.repository.set(offlinePlayer.getUniqueId(), player);
    }

    @Override
    public boolean playerExists(OfflinePlayer offlinePlayer){
        return this.repository.exists(offlinePlayer.getUniqueId());
    }

    @Override
    public List<SleepMostPlayer> getAllPlayers(){
        return this.repository.getAll();
    }
}
