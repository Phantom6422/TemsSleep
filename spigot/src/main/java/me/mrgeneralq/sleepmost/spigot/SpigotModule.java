package me.mrgeneralq.sleepmost.spigot;

import me.mrgeneralq.sleepmost.core.interfaces.IPlatformModule;

/**
 * Spigot/Bukkit platform marker.
 * <p>
 * Bukkit based servers register the command from {@code plugin.yml}, so the
 * shared core command registration path in {@code Sleepmost} handles them. The
 * class exists so the service loader has a platform entry on Spigot as well.
 */
public class SpigotModule implements IPlatformModule {

    @Override
    public void setup() {
        // No platform-specific bootstrap is required on Spigot/Bukkit.
    }
}
