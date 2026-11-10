package me.mrgeneralq.sleepmost.core.eventlisteners;

import me.mrgeneralq.sleepmost.core.interfaces.ISleepService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;

import java.lang.reflect.Method;

public class TimeSkipEventListener implements Listener {

    private static final Method GET_SKIP_REASON = resolveGetSkipReason();

    private final ISleepService sleepService;

    public TimeSkipEventListener(ISleepService sleepService) {
        this.sleepService = sleepService;
    }

    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        if (!this.sleepService.isEnabledAt(event.getWorld())) {
            return;
        }

        if (isNightSkip(event)) {
            event.setCancelled(true);
        }
    }

    private static boolean isNightSkip(TimeSkipEvent event) {
        if (GET_SKIP_REASON == null) {
            return false;
        }

        try {
            Object reason = GET_SKIP_REASON.invoke(event);
            return reason != null && "NIGHT_SKIP".equals(reason.toString());
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    private static Method resolveGetSkipReason() {
        try {
            return TimeSkipEvent.class.getMethod("getSkipReason");
        } catch (NoSuchMethodException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }
}
