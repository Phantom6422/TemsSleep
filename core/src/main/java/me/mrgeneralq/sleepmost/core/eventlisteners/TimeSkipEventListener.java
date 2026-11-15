package me.mrgeneralq.sleepmost.core.eventlisteners;

import me.mrgeneralq.sleepmost.core.interfaces.ISleepService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ClockTimeSkipEvent;
import org.bukkit.event.world.TimeSkipEvent;

public class TimeSkipEventListener implements Listener {

    private final ISleepService sleepService;

    public TimeSkipEventListener(ISleepService sleepService) {
        this.sleepService = sleepService;
    }

    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        if (!this.sleepService.isEnabledAt(event.getWorld())) {
            return;
        }

        // Only take over the skip when TemsSleep actually has tracked sleepers
        // to process. Otherwise the plugin would cancel vanilla forever and
        // leave the world stuck at night.
        if (isNightSkip(event) && this.sleepService.getSleepersAmount(event.getWorld()) > 0) {
            event.setCancelled(true);
        }
    }

    private static boolean isNightSkip(TimeSkipEvent event) {
        return event.getSkipReason() == ClockTimeSkipEvent.SkipReason.NIGHT_SKIP;
    }
}
