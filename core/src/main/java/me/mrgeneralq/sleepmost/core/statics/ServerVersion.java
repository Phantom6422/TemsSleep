package me.mrgeneralq.sleepmost.core.statics;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runtime Minecraft version detection.
 * <p>
 * TemsSleep supports Paper and Purpur 26.3 and newer. Anything older is
 * reported as {@link #UNKNOWN}; the plugin then disables itself instead of
 * running against a partially supported API.
 * <p>
 * Later 26.x releases keep the tested API family ({@link #V26_3}). A newer
 * major release is accepted as {@link #NEWER} so the plugin can still load,
 * but it is logged as untested because future Paper APIs may change.
 */
public enum ServerVersion
{
    UNKNOWN, V26_3, NEWER;

    public static final String MINIMUM_SUPPORTED_VERSION = "26.3";

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)");
    private static final Pattern MC_VERSION_PATTERN = Pattern.compile("MC:\\s*(\\d+)\\.(\\d+)");

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
            this.name = enumName.substring(1).replace('_', '.');
        }
    }

    public String getName() {
        return this.name;
    }

    public boolean isSupported() {
        return this == V26_3 || this == NEWER;
    }

    /**
     * @return true when the server is newer than the version TemsSleep was
     *         tested against (26.3)
     */
    public boolean isNewerThanTested() {
        return this == NEWER;
    }

    /**
     * Parses a Paper/Minecraft version string such as {@code 26.3-R0.1-SNAPSHOT},
     * {@code 26.3.build.248-stable} or {@code git-Paper-123 (MC: 26.3)}.
     *
     * @param rawVersion raw version string, may be {@code null}
     * @return {@link #V26_3} for 26.3-26.x, {@link #NEWER} for newer major
     *         releases, otherwise {@link #UNKNOWN}
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
        try {
            major = Integer.parseInt(matcher.group(1));
            minor = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException ignored) {
            return UNKNOWN;
        }

        // 26.3 is the minimum supported release. Later 26.x releases keep the
        // same API family; newer major releases are accepted but untested.
        if (major == 26 && minor >= 3) {
            return V26_3;
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
