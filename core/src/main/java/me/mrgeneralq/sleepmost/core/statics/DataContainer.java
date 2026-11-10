package me.mrgeneralq.sleepmost.core.statics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class DataContainer {

    private static DataContainer instance;

    private final Set<UUID> runningWorldsAnimation = new HashSet<>();
    private final Map<UUID, Set<UUID>> sleepingPlayers = new HashMap<>();

    private DataContainer(){}

    public static DataContainer getContainer() {
        if (instance == null)
            instance = new DataContainer();
        return instance;
    }

    /**
     * Drops the singleton and all cached player/world state. Called on plugin
     * disable so the plugin class loader is not retained by static state.
     */
    public static void reset() {
        if (instance != null) {
            instance.clearAll();
            instance = null;
        }
    }

    public void setAnimationRunningAt(World world, boolean running) {
        if (world == null) {
            return;
        }
        if (running) {
            this.runningWorldsAnimation.add(world.getUID());
        } else {
            this.runningWorldsAnimation.remove(world.getUID());
        }
    }

    public boolean isAnimationRunningAt(World world) {
        return world != null && this.runningWorldsAnimation.contains(world.getUID());
    }

    public void setPlayerSleeping(Player player, boolean sleeping) {
        if (player == null) {
            return;
        }
        this.setPlayerSleeping(player.getWorld(), player.getUniqueId(), sleeping);
    }

    /**
     * Removes/registers a player in a specific world. This is required when a
     * player changes worlds: the event fires after the world swap, so using the
     * player's current world would leave a stale entry in the previous world.
     */
    public void setPlayerSleeping(World world, UUID playerUUID, boolean sleeping) {
        if (world == null || playerUUID == null) {
            return;
        }

        UUID worldUUID = world.getUID();
        if (sleeping) {
            this.sleepingPlayers.computeIfAbsent(worldUUID, w -> new HashSet<>()).add(playerUUID);
            return;
        }

        Set<UUID> sleepers = this.sleepingPlayers.get(worldUUID);
        if (sleepers == null) {
            return;
        }
        sleepers.remove(playerUUID);
        if (sleepers.isEmpty()) {
            this.sleepingPlayers.remove(worldUUID);
        }
    }

    public boolean isSleeping(World world, UUID playerUUID) {
        if (world == null || playerUUID == null) {
            return false;
        }
        Set<UUID> sleepers = this.sleepingPlayers.get(world.getUID());
        return sleepers != null && sleepers.contains(playerUUID);
    }

    public boolean anyoneSleepingAt(World world) {
        return world != null && getSleepingCount(world) > 0;
    }

    public int getSleepingCount(World world) {
        if (world == null) {
            return 0;
        }
        Set<UUID> sleepers = this.sleepingPlayers.get(world.getUID());
        return sleepers == null ? 0 : sleepers.size();
    }

    public List<Player> getSleepingPlayers(World world) {
        if (world == null) {
            return Collections.emptyList();
        }
        Set<UUID> sleepers = this.sleepingPlayers.get(world.getUID());
        if (sleepers == null || sleepers.isEmpty()) {
            return Collections.emptyList();
        }
        return sleepers.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    public void clearSleepingPlayers(World world) {
        if (world != null) {
            this.sleepingPlayers.remove(world.getUID());
            this.runningWorldsAnimation.remove(world.getUID());
        }
    }

    public void clearAll() {
        this.sleepingPlayers.clear();
        this.runningWorldsAnimation.clear();
    }
}
