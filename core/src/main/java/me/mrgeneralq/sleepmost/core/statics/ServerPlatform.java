package me.mrgeneralq.sleepmost.core.statics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Detects the server software TemsSleep is running on.
 * <p>
 * Paper and Purpur are both fully supported. Purpur additionally exposes
 * {@link Player#isAfk()} in its API; when that method is present it is used for
 * the {@code use-afk} sleep-count exemption, so Purpur servers do not need
 * PlaceholderAPI plus Essentials for native AFK detection.
 */
public enum ServerPlatform {
    PAPER("Paper"),
    PURPUR("Purpur"),
    UNKNOWN("unknown");

    private static final ServerPlatform CURRENT = detect();
    private static final Method PLAYER_IS_AFK = resolveAfkMethod();

    private final String displayName;

    ServerPlatform(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static ServerPlatform get() {
        return CURRENT;
    }

    /**
     * @return true when the server exposes Purpur's native AFK state
     */
    public static boolean hasNativeAfk() {
        return CURRENT == PURPUR && PLAYER_IS_AFK != null;
    }

    /**
     * Reads Purpur's native AFK state. On Paper this always returns false and
     * the configured PlaceholderAPI-based check is used instead.
     */
    public static boolean isPlayerAfk(Player player) {
        if (!hasNativeAfk() || player == null) {
            return false;
        }
        try {
            return (boolean) PLAYER_IS_AFK.invoke(player);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            // Never drop a player from the sleep count because an optional API changed.
            return false;
        }
    }

    private static ServerPlatform detect() {
        try {
            String name = Bukkit.getName();
            if (containsIgnoreCase(name, "purpur")) {
                return PURPUR;
            }
            if (containsIgnoreCase(name, "paper")) {
                return PAPER;
            }
            String version = Bukkit.getVersion();
            if (containsIgnoreCase(version, "purpur")) {
                return PURPUR;
            }
            if (containsIgnoreCase(version, "paper")) {
                return PAPER;
            }
        } catch (RuntimeException | LinkageError ignored) {
            // No running server (e.g. unit tests).
        }
        return UNKNOWN;
    }

    private static boolean containsIgnoreCase(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private static Method resolveAfkMethod() {
        try {
            return Player.class.getMethod("isAfk");
        } catch (NoSuchMethodException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }
}
