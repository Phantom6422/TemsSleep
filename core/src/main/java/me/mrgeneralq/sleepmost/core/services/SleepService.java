package me.mrgeneralq.sleepmost.core.services;

import de.myzelyam.api.vanish.VanishAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.mrgeneralq.sleepmost.core.Sleepmost;
import me.mrgeneralq.sleepmost.core.enums.SleepMostHook;
import me.mrgeneralq.sleepmost.core.enums.SleepState;
import me.mrgeneralq.sleepmost.core.events.PlayerSleepStateChangeEvent;
import me.mrgeneralq.sleepmost.core.interfaces.*;
import me.mrgeneralq.sleepmost.core.models.Hook;
import me.mrgeneralq.sleepmost.core.models.SleepMostWorld;
import me.mrgeneralq.sleepmost.core.runnables.NightcycleAnimationTask;
import me.mrgeneralq.sleepmost.core.statics.DataContainer;
import me.mrgeneralq.sleepmost.core.enums.SleepSkipCause;
import me.mrgeneralq.sleepmost.core.events.SleepSkipEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;

import static me.mrgeneralq.sleepmost.core.enums.SleepSkipCause.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SleepService implements ISleepService {

    private final Sleepmost main;
    private final IConfigRepository configRepository;
    private final IConfigService configService;
    private final IFlagsRepository flagsRepository;
    private final IFlagService flagService;
    private final IPlayerService playerService;
    private final IDebugService debugService;
    private final DataContainer dataContainer = DataContainer.getContainer();
    private final ISleepMostWorldService sleepMostWorldService;
    private final IHookService hookService;
    private final IMessageService messageService;

    private static final int
            DEFAULT_NIGHT_START_TIME = 12541,
            DEFAULT_NIGHT_END_TIME = 23460,
            TICKS_PER_DAY = 24000;

    // Resolved once: looking up the method for every sleep-count calculation is wasteful.
    private static final Method AFK_METHOD = resolveAfkMethod();

    private int nightStartTime;
    private int nightEndTime;

    public SleepService(
            Sleepmost main,
            IConfigService configService,
            IConfigRepository configRepository,
            IFlagsRepository flagsRepository,
            IFlagService flagService,
            IPlayerService playerService,
            IDebugService debugService,
            ISleepMostWorldService sleepMostWorldService,
            IHookService hookService,
            IMessageService messageService
    ) {

        this.main = main;
        this.configService = configService;
        this.configRepository = configRepository;
        this.flagsRepository = flagsRepository;
        this.flagService = flagService;
        this.playerService = playerService;
        this.debugService = debugService;
        this.sleepMostWorldService = sleepMostWorldService;
        this.hookService = hookService;
        this.messageService = messageService;
        refreshTimeSettings();
    }

    @Override
    public void reloadConfig() {
        configRepository.reload();
        refreshTimeSettings();
    }

    @Override
    public boolean isEnabledAt(World world) {
        return configRepository.containsWorld(world);
    }

    @Override
    public void enableAt(World world) {
        configRepository.enableForWorld(world);
    }

    @Override
    public void disableAt(World world) {
        configRepository.disableForWorld(world);
    }

    @Override
    public int getSleepersAmount(World world) {
        return this.dataContainer.getSleepingCount(world);
    }

    @Override
    public List<Player> getSleepers(World world){
        return new ArrayList<>(this.dataContainer.getSleepingPlayers(world));
    }

    @Override
    public int getPlayerCountInWorld(World world) {

        List<Player> playersList = world.getPlayers();
        List<Player> filteredPlayers = playersList;
        final boolean debug = this.debugService.isEnabled();

        if (debug) {
            this.debugService.print("");
            this.debugService.print("=============================");
            this.debugService.print(String.format("&f[&b%s&f] &fTotal players [&e%s&f]: %s", world.getName(), playersList.size(), getJoinedStream(playersList, filteredPlayers)));
        }

        final boolean useAfk = this.flagsRepository.getUseAfkFlag().getValueAt(world);

        //AFK DETECTION (reflection)
        if(useAfk && AFK_METHOD != null){

            final Method isAfkMethod = AFK_METHOD;
            filteredPlayers = filteredPlayers.stream().filter(p -> {
                try {
                    return !(boolean) isAfkMethod.invoke(p);
                } catch (ReflectiveOperationException ignored) {
                    // Never drop a player because an optional API changed.
                    return true;
                }
            }).collect(Collectors.toList());

            if (debug) {
                this.debugService.print(String.format("&f[&b%s&f] &fNon AFK (server) [&e%s&f]: %s", world.getName(), playersList.size(), getJoinedStream(playersList, filteredPlayers)));
            }
        }

        Optional<Hook> superVanishHook = hookService.getHook(SleepMostHook.SUPER_VANISH);
        if(superVanishHook.isPresent()){
            filteredPlayers = filteredPlayers.stream().filter(p -> !VanishAPI.isInvisible(p)).collect(Collectors.toList());
            if (debug) {
                this.debugService.print(String.format("&f[&b%s&f] &fVisible players (super vanish) [&e%s&f]: %s", world.getName(), playersList.size(), getJoinedStream(playersList, filteredPlayers)));
            }
        }

        //exclude fake players
        filteredPlayers = filteredPlayers.stream().filter(this.playerService::isRealPlayer).collect(Collectors.toList());
        if (debug) {
            this.debugService.print(String.format("&f[&b%s&f] &fReal players [&e%s&f]: %s", world.getName(), playersList.size(), getJoinedStream(playersList, filteredPlayers)));
        }

        // If flag is active, ignore players in spectator mode from sleep count.
        if (flagsRepository.getExemptSpectatorFlag().getValueAt(world)){
            filteredPlayers = filteredPlayers.stream().filter(p -> p.getGameMode() != GameMode.SPECTATOR).collect(Collectors.toList());
            if (debug) {
                this.debugService.print(String.format("&f[&b%s&f] &fNon-spectator players [&e%s&f]: %s", world.getName(), playersList.size(), getJoinedStream(playersList, filteredPlayers)));
            }
        }

        // If flag is active, ignore players in creative mode from sleep count.
        if(flagsRepository.getExemptCreativeFlag().getValueAt(world)){
            filteredPlayers = filteredPlayers.stream().filter(p -> p.getGameMode() != GameMode.CREATIVE).collect(Collectors.toList());
            if (debug) {
                this.debugService.print(String.format("&f[&b%s&f] &fNon-creative players [&e%s&f]: %s", world.getName(), playersList.size(), getJoinedStream(playersList, filteredPlayers)));
            }
        }

        if(flagsRepository.getUseExemptFlag().getValueAt(world)){
            filteredPlayers = filteredPlayers.stream().filter(p -> !p.hasPermission("temssleep.exempt") && !p.hasPermission("sleepmost.exempt")).collect(Collectors.toList());
            if (debug) {
                this.debugService.print(String.format("&f[&b%s&f] &fMissing bypass permission (temssleep.exempt) players [&e%s&f]: %s", world.getName(), playersList.size(), getJoinedStream(playersList, filteredPlayers)));
            }
        }

        if(flagsRepository.getExemptFlyingFlag().getValueAt(world)){
            filteredPlayers = filteredPlayers.stream().filter(p -> !p.isFlying()).collect(Collectors.toList());
            if (debug) {
                this.debugService.print(String.format("&f[&b%s&f] &fNon-flying players [&e%s&f]: %s", world.getName(), playersList.size(), getJoinedStream(playersList, filteredPlayers)));
            }
        }

         if (useAfk && flagService.isAfkFlagUsable()){

             String afkPlaceholder = this.configService.getAFKPlaceholder();
             String afkPositiveResult = this.configService.getAFKPositiveResult();

             filteredPlayers = filteredPlayers.stream()
                     .filter(p -> !PlaceholderAPI.setPlaceholders(p, afkPlaceholder).equalsIgnoreCase(afkPositiveResult))
                     .collect(Collectors.toList());
             if (debug) {
                 this.debugService.print(String.format("&f[&b%s&f] &fNon-AFK players [&e%s&f]: %s", world.getName(), playersList.size(), getJoinedStream(playersList, filteredPlayers)));
             }
         }

        int belowYFlagValue = flagsRepository.getExemptBelowYFlag().getValueAt(world);
         if(belowYFlagValue > -1){
             filteredPlayers = filteredPlayers.stream().filter(p -> p.getLocation().getY() > belowYFlagValue).collect(Collectors.toList());
             if (debug) {
                 this.debugService.print(String.format("&f[&b%s&f] &fPlayers above &b%s&fY-coord [&e%s&f]: %s", world.getName(), belowYFlagValue, playersList.size(), getJoinedStream(playersList, filteredPlayers)));
             }
         }

         int playerCount = filteredPlayers.size();
         int endResult = playerCount == 0 ? 1 : playerCount;

        if (debug) {
            this.debugService.print(String.format("&f[&b%s&f] &fCalculated end TOTAL [&e%s&f]: %s", world.getName(), endResult, getJoinedStream(playersList, filteredPlayers)));
            this.debugService.print("=============================");
            this.debugService.print("");
        }

        return endResult;
    }

    @Override
    public double getSleepersPercentage(World world) {
        int required = getRequiredSleepersCount(world);
        if (required <= 0) {
            return 0D;
        }
        return (double) getSleepersAmount(world) / (double) required;
    }

    @Override
    public int getRequiredSleepersCount(World world) {

        // Calculate the player count once; each call performs permission, AFK and
        // visibility checks, and the old code called it up to three times.
        int playerCount = getPlayerCountInWorld(world);

        switch (this.flagsRepository.getCalculationMethodFlag().getValueAt(world)) {

            case PERCENTAGE_REQUIRED:
                double percentageRequired = this.flagsRepository.getPercentageRequiredFlag().getValueAt(world);
                return Math.max((int) Math.ceil(playerCount * percentageRequired), 0);

            case PLAYERS_REQUIRED:
                int requiredPlayers = this.flagsRepository.getPlayersRequiredFlag().getValueAt(world);
                return Math.max(Math.min(requiredPlayers, playerCount), 0);

            default:
                return 1;
        }
    }

    @Override
    public SleepSkipCause getCurrentSkipCause(World world) {

        if(isNight(world))
            return NIGHT_TIME;

        if(world.isThundering())
            return STORM;

        return UNKNOWN;
    }

    @Override
    public boolean isRequiredCountReached(World world) {
        return this.getSleepersAmount(world) >= this.getRequiredSleepersCount(world);
    }

    @Override
    public int getRemainingSleepers(World world){
        return Math.max(this.getRequiredSleepersCount(world) - this.getSleepersAmount(world), 0);
    }

    @Override
    public void clearSleepersAt(World world)
    {
        this.dataContainer.clearSleepingPlayers(world);
    }

    @Override
    public void setSleeping(Player player, boolean sleeping) {

        SleepState sleepState = (sleeping) ? SleepState.SLEEPING: SleepState.AWAKE;

        PlayerSleepStateChangeEvent sleepStateChangeEvent = new PlayerSleepStateChangeEvent(player, sleepState);
        this.dataContainer.setPlayerSleeping(player, sleeping);

        Bukkit.getPluginManager().callEvent(sleepStateChangeEvent);

    }

    @Override
    public boolean isPlayerAsleep(Player player) {
        if (player == null) {
            return false;
        }
        return this.dataContainer.isSleeping(player.getWorld(), player.getUniqueId());
    }


    @Override
    public boolean isSleepingPossible(World world) {
        return isNight(world) || world.isThundering();
    }

    @Override
    public boolean isNight(World world) {
        long time = world.getTime();
        if (nightStartTime <= nightEndTime) {
            return time >= nightStartTime && time < nightEndTime;
        }
        // A wrap-around range (e.g. night-end < night-start) is valid too.
        return time >= nightStartTime || time < nightEndTime;
    }

    @Override
    public void executeSleepReset(World world, String lastSleeperName, String lastSleeperDisplayName, List<OfflinePlayer> peopleWhoSlept, SleepSkipCause skipCause) {
        // The animation finishes at dawn, when isNight() is already false, so the
        // cause also decides whether the configured reset time has to be applied.
        if(skipCause == NIGHT_TIME || isNight(world))
            world.setTime(configService.getResetTime());

        if(this.flagsRepository.getSkipStormFlag().getValueAt(world) || skipCause == SleepSkipCause.STORM){
            world.setThundering(false);
            world.setStorm(false);
        }

        Bukkit.getServer().getPluginManager().callEvent(new SleepSkipEvent(world,peopleWhoSlept ,skipCause, lastSleeperName, lastSleeperDisplayName));
    }

    @Override
    public boolean shouldSkip(World world) {
        return isEnabledAt(world) &&
                isSleepingPossible(world) &&
                isRequiredCountReached(world);
    }

    @Override
    public void runSkipAnimation(Player player, SleepSkipCause sleepSkipCause) {
        World world = player.getWorld();

        SleepMostWorld sleepMostWorld = this.sleepMostWorldService.getWorld(world);
        if (sleepMostWorld == null) {
            return;
        }

        // Guard against the same world starting a second animation. The old
        // DataContainer flag was never populated, so two players could start
        // overlapping tasks that both advanced time and fired reset events.
        if (this.dataContainer.isAnimationRunningAt(world) || sleepMostWorld.isTimeCycleAnimationIsRunning())
            return;

        List<OfflinePlayer> sleepingPlayers = this.getSleepers(world).stream().map(p -> Bukkit.getOfflinePlayer(p.getUniqueId())).collect(Collectors.toList());

        this.dataContainer.setAnimationRunningAt(world, true);
        sleepMostWorld.setTimeCycleAnimationIsRunning(true);

        new NightcycleAnimationTask(this, this.flagsRepository, world, player, sleepingPlayers , sleepSkipCause, sleepMostWorldService, messageService, configService).runTaskTimer(this.main, 0, 1);
    }

    private void refreshTimeSettings() {
        int start = configService.getNightStartTime();
        int end = configService.getNightStopTime();

        if (start < 0 || end < 0 || start == end) {
            start = DEFAULT_NIGHT_START_TIME;
            end = DEFAULT_NIGHT_END_TIME;
        }

        start = Math.floorMod(start, TICKS_PER_DAY);
        end = Math.floorMod(end, TICKS_PER_DAY);

        if (start == end) {
            start = DEFAULT_NIGHT_START_TIME;
            end = DEFAULT_NIGHT_END_TIME;
        }

        this.nightStartTime = start;
        this.nightEndTime = end;
    }

    private static Method resolveAfkMethod() {
        try {
            return Player.class.getMethod("isAfk");
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private String getJoinedStream(List<Player> playersList, List<Player> newPlayersList){

        Set<UUID> included = new HashSet<>();
        for (Player player : newPlayersList) {
            included.add(player.getUniqueId());
        }

        List<String> names = new ArrayList<>(playersList.size());

        for(Player previousPlayer: playersList){
            if(included.contains(previousPlayer.getUniqueId()))
                names.add(ChatColor.GREEN + previousPlayer.getName() + ChatColor.RESET);
            else
                names.add(ChatColor.RED + previousPlayer.getName() + ChatColor.RESET);

        }

        return String.join(",", names);
    }
}
