package me.mrgeneralq.sleepmost.core.statics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Runtime Minecraft/Paper version detection.
 * <p>
 * The server version is parsed from {@link Bukkit#getBukkitVersion()} instead of
 * matching substrings of {@link Bukkit#getVersion()}. Substring matching breaks
 * for the year based Minecraft version scheme (26.x), which made every capability
 * flag evaluate to false on modern servers.
 */
public enum ServerVersion
{
    UNKNOWN, V1_8, V1_9, V1_10, V1_11, V1_12, V1_13, V1_14, V1_15, V1_16, V1_17, V1_18, V1_19, V1_20, V1_21,
    V26_1, V26_2, V26_3;

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)");
    private static final Pattern MC_VERSION_PATTERN = Pattern.compile("MC:\\s*(\\d+)\\.(\\d+)");

    private final String name;
    private MaxHPHealer maxHPHealer;
    private boolean supportsHexColors;
    private boolean supportsTitles;
    private boolean supportsBossBars;
    private boolean hasTimeSkipEvent;
    private boolean supportsClickableText;
    private boolean supportsGameRules;

    public static final ServerVersion CURRENT_VERSION;

    /**
     * Default download page. The live update checker uses the repository
     * configured in {@code config.yml} instead.
     */
    public static final String UPDATE_URL = "https://github.com/Phantom6422/TemsSleep/releases/latest";

    //cached for internal usage
    private static final ServerVersion[] VALUES = values();

    static {
        CURRENT_VERSION = computeServerVersion();
        forVersionsFrom(V1_16, version -> version.supportsHexColors = true);
        forVersionsFrom(V1_12, version -> version.supportsTitles = true);
        forVersionsFrom(V1_17, version -> version.supportsClickableText = true);
        forVersionsUntil(V1_9, version -> version.maxHPHealer = MaxHPHealer.LEGACY_HEALER);
        forVersionsFrom(V1_9, version -> version.maxHPHealer = MaxHPHealer.UPDATED_HEALER);
        forVersionsFrom(V1_15, version -> version.hasTimeSkipEvent = true);
        forVersionsFrom(V1_12, version -> version.supportsBossBars = true);
        forVersionsFrom(V1_13, version -> version.supportsGameRules = true);
    }

    ServerVersion() {
        String enumName = name();
        this.name = "UNKNOWN".equals(enumName) ? "unknown" : enumName.substring(1).replace('_', '.');
    }

    public String getName() {
        return this.name;
    }

    public boolean supportsHexColors() {
        return this.supportsHexColors;
    }

    public boolean supportsTitles() {
        return this.supportsTitles;
    }

    public void healToMaxHP(Player player) {
        this.maxHPHealer.heal(player);
    }

    public boolean supportsBossBars(){
        return this.supportsBossBars;
    }

    public boolean hasTimeSkipEvent() {
        return hasTimeSkipEvent;
    }

    public boolean supportsClickableText() {
        return supportsClickableText;
    }

    public boolean supportsGameRules() {
        return supportsGameRules;
    }

    /**
     * Parses a Bukkit/Minecraft version string such as {@code 1.21.11-R0.1-SNAPSHOT}
     * or {@code 26.3-R0.1-SNAPSHOT}.
     *
     * @param rawVersion raw version string, may be {@code null}
     * @return the closest known version, never {@code null}
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

        if (major == 1) {
            if (minor < 8) {
                return UNKNOWN;
            }
            if (minor >= 21) {
                return V1_21;
            }
            switch (minor) {
                case 8: return V1_8;
                case 9: return V1_9;
                case 10: return V1_10;
                case 11: return V1_11;
                case 12: return V1_12;
                case 13: return V1_13;
                case 14: return V1_14;
                case 15: return V1_15;
                case 16: return V1_16;
                case 17: return V1_17;
                case 18: return V1_18;
                case 19: return V1_19;
                case 20: return V1_20;
                default: return UNKNOWN;
            }
        }

        if (major >= 26) {
            if (minor >= 3) {
                return V26_3;
            }
            if (minor == 2) {
                return V26_2;
            }
            return V26_1;
        }

        return UNKNOWN;
    }

    private static ServerVersion computeServerVersion() {
        try {
            ServerVersion parsed = parse(Bukkit.getBukkitVersion());
            if (parsed != UNKNOWN) {
                return parsed;
            }
            // Paper occasionally exposes only the "MC: x.y" marker through getVersion().
            return parse(Bukkit.getVersion());
        } catch (RuntimeException | LinkageError ignored) {
            // No running server (e.g. unit tests) or an incompatible API.
            return UNKNOWN;
        }
    }

    private static void forVersionsFrom(ServerVersion minimum, Consumer<ServerVersion> action) {
        versionsStream()
                .filter(version -> version.ordinal() >= minimum.ordinal())
                .forEach(action);
    }

    private static void forVersionsUntil(ServerVersion maximum, Consumer<ServerVersion> action) {
        versionsStream()
                .filter(version -> version.ordinal() < maximum.ordinal())
                .forEach(action);
    }

    private static Stream<ServerVersion> versionsStream() {
        return Arrays.stream(VALUES)
                .filter(version -> version != UNKNOWN);
    }
}
