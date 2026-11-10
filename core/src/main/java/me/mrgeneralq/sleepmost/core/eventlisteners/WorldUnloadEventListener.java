package me.mrgeneralq.sleepmost.core.eventlisteners;

import me.mrgeneralq.sleepmost.core.interfaces.IBossBarService;
import me.mrgeneralq.sleepmost.core.interfaces.ISleepMostWorldService;
import me.mrgeneralq.sleepmost.core.statics.DataContainer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldUnloadEventListener implements Listener {

    private final IBossBarService bossBarService;
    private final ISleepMostWorldService sleepMostWorldService;

    public WorldUnloadEventListener(IBossBarService bossBarService, ISleepMostWorldService sleepMostWorldService) {
        this.bossBarService = bossBarService;
        this.sleepMostWorldService = sleepMostWorldService;
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent e) {

        World world = e.getWorld();

        if (this.bossBarService != null) {
            this.bossBarService.unregisterBossBar(world);
        }
        DataContainer.getContainer().clearSleepingPlayers(world);
        this.sleepMostWorldService.unregisterWorld(world);
    }
}
