package me.mrgeneralq.sleepmost.core.eventlisteners;

import me.mrgeneralq.sleepmost.core.interfaces.IBossBarService;
import me.mrgeneralq.sleepmost.core.interfaces.ISleepMostWorldService;
import me.mrgeneralq.sleepmost.core.statics.ServerVersion;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadEventListener implements Listener {

    private final IBossBarService bossBarService;
    private final ISleepMostWorldService sleepMostWorldService;

    public WorldLoadEventListener(IBossBarService bossBarService, ISleepMostWorldService sleepMostWorldService) {
        this.bossBarService = bossBarService;
        this.sleepMostWorldService = sleepMostWorldService;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e){

        World world = e.getWorld();

        if(ServerVersion.CURRENT_VERSION.supportsBossBars() && this.bossBarService != null){
            this.bossBarService.registerBossBar(world);
        }

        if(!this.sleepMostWorldService.worldExists(world)){
            this.sleepMostWorldService.registerWorld(world);
        }

    }
}
