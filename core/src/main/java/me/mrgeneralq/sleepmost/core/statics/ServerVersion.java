package me.mrgeneralq.sleepmost.core.statics;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runtime Minecraft version detection.
 * <p>
 * TemsSleep supports all Paper and Purpur 26.x releases from 26.1.1 onward.
 * Anything older is reported as {@link #UNKNOWN}; the plugin then disables
 * itself instead of running against a partially supported API.
 * <p>
 * Every 26.x release shares the tested API family ({@link #V26_X}). A newer
 * major release is detected as {@link #NEWER} but is not enabled until it has
 * been validated against that API.
 */
public enum ServerVersion
{
    UNKNOWN, V26_X, NEWER;

    public static final String MINIMUM_SUPPORTED_VERSION = "26.1.1";

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
    private static final Pattern MC_VERSION_PATTERN = Pattern.compile("MC:\\s*(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    public static final ServerVersion CURRENT_VERSION;

    static {
        CURRENT_VERSION = computeServerVersion();
    }

    private final String name;

    ServerVersion() {
        String enumName = name();
        if ("UNKNOWN".equals(enumName)) {
            this.name = "unknown";
        } else if ("NEWER".equals(enumName)) {
            this.name = "newer";
        } else {
            this.name = "26.x";
        }
    }

    public String getName() {
        return this.name;
    }

    public boolean isSupported() {
        return this == V26_X;
    }

    /**
     * @return true when the server is a newer major release than the tested
     *         26.x line
     */
    public boolean isNewerThanTested() {
        return this == NEWER;
    }

    /**
     * Parses a Paper/Minecraft version string such as {@code 26.1.2-...},
     * {@code 26.3.build.248-stable} or {@code git-Paper-123 (MC: 26.2)}.
     *
     * @param rawVersion raw version string, may be {@code null}
     * @return {@link #V26_X} for any 26.x release, {@link #NEWER} for a newer
     *         major release, otherwise {@link #UNKNOWN}
     */
    public static ServerVersion parse(String rawVersion) {
        if (rawVersion == null || rawVersion.isEmpty()) {
            return UNKNOWN;
        }

        Matcher matcher = VERSION_PATTERN.matcher(rawVersion);
        if (!matcher.find()) {
            matcher = MC_VERSION_PATTERN.matcher(rawVersion);
            if (!matcher.find()) {
                return UNKNOWN;
            }
        }

        final int major;
        final int minor;
        final int patch;
        try {
            major = Integer.parseInt(matcher.group(1));
            minor = Integer.parseInt(matcher.group(2));
            patch = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        } catch (NumberFormatException ignored) {
            return UNKNOWN;
        }

        // The 26.x line starts at 26.1.1. Every 26.x release uses the same
        // tested API family; newer major releases are detected but not enabled.
        if (major == 26 && (minor > 1 || (minor == 1 && patch >= 1))) {
            return V26_X;
        }
        return major > 26 ? NEWER : UNKNOWN;
    }

    private static ServerVersion computeServerVersion() {
        try {
            ServerVersion parsed = parse(Bukkit.getMinecraftVersion());
            if (parsed != UNKNOWN) {
                return parsed;
            }
            parsed = parse(Bukkit.getBukkitVersion());
            if (parsed != UNKNOWN) {
                return parsed;
            }
            // Some forks only expose the "MC: x.y" marker through getVersion().
            return parse(Bukkit.getVersion());
        } catch (RuntimeException | LinkageError ignored) {
            // No running server (e.g. unit tests) or an incompatible API.
            return UNKNOWN;
        }
    }
}
