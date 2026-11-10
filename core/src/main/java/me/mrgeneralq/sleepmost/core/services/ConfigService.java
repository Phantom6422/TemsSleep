package me.mrgeneralq.sleepmost.core.services;

import me.mrgeneralq.sleepmost.core.interfaces.IConfigService;
import me.mrgeneralq.sleepmost.core.Sleepmost;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class ConfigService implements IConfigService {

    private static final String DEFAULT_UPDATE_REPOSITORY = "Phantom6422/TemsSleep";
    private static final java.util.regex.Pattern UPDATE_REPOSITORY_PATTERN =
            java.util.regex.Pattern.compile("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+");

    private final Sleepmost main;

    public ConfigService(Sleepmost main) {
        this.main = main;
    }

    @Override
    public int getResetTime() {
        return main.getConfig().getInt("time-after-reset", 0);
    }

    @Override
    public boolean updateCheckerEnabled() {
        return main.getConfig().getBoolean("update-checker-enabled", true);
    }

    @Override
    public String getUpdateCheckerRepository() {
        String repository = main.getConfig().getString("update-checker-github-repository", DEFAULT_UPDATE_REPOSITORY);
        if (repository == null || !UPDATE_REPOSITORY_PATTERN.matcher(repository).matches()) {
            return DEFAULT_UPDATE_REPOSITORY;
        }
        return repository;
    }


    @Override
    public Set<World> getEnabledWorlds()
    {
        ConfigurationSection sleepSection = main.getConfig().getConfigurationSection("sleep");
        if (sleepSection == null) {
            return Set.of();
        }
        return sleepSection.getKeys(false).stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    @Override
    public boolean debugModeEnabled() {
        return main.getConfig().getBoolean("debug-mode");
    }

    @Override
    public int getNightcycleAnimationSpeed() {
        return Math.max(main.getConfig().getInt("nightcycle-animation-speed", 85), 1);
    }

    @Override
    public int getNightcycleAnimationSpeedMax() {
        int max = main.getConfig().getInt("nightcycle-animation-speed-max", 170);
        return Math.max(max, getNightcycleAnimationSpeed());
    }

    @Override
    public int getNightStartTime() {
        return main.getConfig().getInt("time.night-start", 12542);
    }

    @Override
    public int getNightStopTime() {
        return main.getConfig().getInt("time.night-end", 23850);
    }

    @Override
    public String getAFKPlaceholder() {
        return main.getConfig().getString("afk-handling.placeholder", "%essentials_afk%");
    }

    @Override
    public String getAFKPositiveResult() {
        return main.getConfig().getString("afk-handling.positive-result", "yes");
    }

}
