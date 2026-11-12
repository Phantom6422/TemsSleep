package me.mrgeneralq.sleepmost.core.runnables;

import me.mrgeneralq.sleepmost.core.enums.MessageKey;
import me.mrgeneralq.sleepmost.core.interfaces.*;
import me.mrgeneralq.sleepmost.core.models.SleepMostWorld;
import me.mrgeneralq.sleepmost.core.statics.DataContainer;
import me.mrgeneralq.sleepmost.core.enums.SleepSkipCause;
import me.mrgeneralq.sleepmost.core.statics.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class NightcycleAnimationTask extends BukkitRunnable {

    /**
     * Titles are re-sent periodically so they stay visible for the whole
     * animation. Sending one title packet per tick (the old behaviour) spammed
     * the client and made the animation flicker.
     */
    private static final int CLOCK_TITLE_INTERVAL_TICKS = 2;

    /**
     * Dawn is the end of the night animation. The sleep window can end a little
     * earlier (config time.night-end), but the animation should visually run all
     * the way to 6:00 rather than jumping from ~5:30 to morning.
     */
    private static final int ANIMATION_DAWN_TIME = 24000;

    /**
     * After this time (~4:00 AM) a started night animation always finishes.
     * Stopping near dawn leaves the world at ~5:30, skips the final reset and
     * prevents the "Good morning" message.
     */
    private static final int COMMIT_TO_DAWN_TIME = 22000;

    private final ISleepService sleepService;
    private final DataContainer dataContainer = DataContainer.getContainer();
    private final ISleepMostWorldService sleepMostWorldService;
    private final IConfigService configService;
    private final IMessageService messageService;
    private final IFlagsRepository flagsRepository;
    private final World world;
    private final String lastSleeperName;
    private final String lastSleeperDisplayName;
    private final SleepSkipCause skipCause;
    private final List<OfflinePlayer> peopleWhoSlept;
    private int iterationCount = 0;

    public NightcycleAnimationTask(ISleepService sleepService, IFlagsRepository flagsRepository, World world, Player lastSleeper, List<OfflinePlayer> peopleWhoSlept, SleepSkipCause sleepSkipCause, ISleepMostWorldService sleepMostWorldService, IMessageService messageService, IConfigService configService) {
        this.sleepService = sleepService;
        this.flagsRepository = flagsRepository;
        this.world = world;
        this.lastSleeperName = lastSleeper.getName();
        this.lastSleeperDisplayName = lastSleeper.getDisplayName();
        this.skipCause = sleepSkipCause;
        this.peopleWhoSlept = peopleWhoSlept;

        this.sleepMostWorldService = sleepMostWorldService;
        this.messageService = messageService;
        this.configService = configService;
    }

    @Override
    public void run() {
        if (Bukkit.getWorld(this.world.getUID()) == null) {
            stopAnimation();
            return;
        }

        this.iterationCount++;

        //85 by default
        final int baseSpeed = configService.getNightcycleAnimationSpeed();
        int calculatedSpeed = baseSpeed;
        int requiredSleepers = -1;

        if(this.flagsRepository.getDynamicAnimationSpeedFlag().getValueAt(world)){

            int sleepingPlayers = this.sleepService.getSleepersAmount(world);
            requiredSleepers = this.sleepService.getRequiredSleepersCount(world);
            int totalPlayers = world.getPlayers().size();

            int numerator = Math.max(sleepingPlayers - requiredSleepers, 0);
            int denominator = Math.max(totalPlayers - requiredSleepers, 1);
            double additionalPlayersRatio = (double)numerator / (double)denominator;

            int maxSpeed = Math.max(configService.getNightcycleAnimationSpeedMax(), baseSpeed);
            calculatedSpeed = Math.min((int) Math.round((additionalPlayersRatio * (maxSpeed - baseSpeed)) + baseSpeed), maxSpeed);
        }

        try {
            world.setTime(world.getTime() + calculatedSpeed);
        } catch (RuntimeException exception) {
            // The world was unloaded while the task was queued.
            stopAnimation();
            return;
        }

        SleepMostWorld sleepMostWorld = this.sleepMostWorldService.getWorld(world);
        if (sleepMostWorld == null) {
            stopAnimation();
            return;
        }

        final boolean night = isAnimationNight(world);

        // Cancel early only when the animation has not committed to dawn yet.
        // Once a night animation is close to morning, finish it so the world is
        // reset and the morning message is delivered.
        if(!this.flagsRepository.getForceNightcycleAnimationFlag().getValueAt(world)
                && night
                && world.getTime() < COMMIT_TO_DAWN_TIME){
            if (requiredSleepers < 0) {
                requiredSleepers = sleepService.getRequiredSleepersCount(world);
            }
            if (sleepService.getSleepersAmount(world) < requiredSleepers) {
                stopAnimation();
                return;
            }
        }

        if(!night){
            // Clear the animation flag BEFORE firing the skip event: the event
            // listeners intentionally ignore skip events while an animation is
            // running, and they need to process this final one.
            stopAnimation();

            this.sleepService.executeSleepReset(world, this.lastSleeperName, this.lastSleeperDisplayName, this.peopleWhoSlept, this.skipCause);

            if(this.flagsRepository.getClockAnimationFlag().getValueAt(world)
                    && ServerVersion.CURRENT_VERSION.supportsTitles()
                    && this.skipCause == SleepSkipCause.NIGHT_TIME){
                sendTitles(configService.getResetTime());
            }
            return;
        }

        /* **************************************************
         *  All code below runs during the night
         ***************************************************/

        if(this.flagsRepository.getClockAnimationFlag().getValueAt(world)
                && ServerVersion.CURRENT_VERSION.supportsTitles()
                && (this.iterationCount == 1 || this.iterationCount % CLOCK_TITLE_INTERVAL_TICKS == 0)){
            sendTitles((int) world.getTime());
        }
    }

    private boolean isAnimationNight(World world) {
        // A night skip must run through dawn even when the configurable sleep
        // window ends earlier (the default night-end is around 5:51 AM).
        if (this.skipCause == SleepSkipCause.NIGHT_TIME) {
            long time = world.getTime();
            return time >= 12000 && time < ANIMATION_DAWN_TIME;
        }
        return this.sleepService.isNight(world);
    }

    private void sendTitles(int time) {
        String clockTitle = this.messageService.getMessage(MessageKey.CLOCK_TITLE)
                .setTime(time)
                .build();
        String clockSubTitle = this.messageService.getMessage(MessageKey.CLOCK_SUBTITLE)
                .build();

        if (clockTitle.isEmpty() && clockSubTitle.isEmpty()) {
            return;
        }

        List<Player> playerList = (flagsRepository.getNonSleepingClockAnimationFlag().getValueAt(world) ?
                new ArrayList<>(world.getPlayers()) :
                getOnlineSleepers());

        for(Player p: playerList){
            if (p.isOnline()) {
                // Keep a short fade-out: a 10-tick hold with a 4-tick fade is
                // quick enough not to strobe while the clock refreshes every
                // couple of ticks.
                p.sendTitle(clockTitle, clockSubTitle, 0, 10, 4);
            }
        }
    }

    private List<Player> getOnlineSleepers() {
        List<Player> players = new ArrayList<>(this.peopleWhoSlept.size());
        for (OfflinePlayer offlinePlayer : this.peopleWhoSlept) {
            if (!offlinePlayer.isOnline()) {
                continue;
            }
            Player player = offlinePlayer.getPlayer();
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    private void stopAnimation() {
        SleepMostWorld sleepMostWorld = this.sleepMostWorldService.getWorld(world);
        if (sleepMostWorld != null) {
            sleepMostWorld.setTimeCycleAnimationIsRunning(false);
        }
        this.dataContainer.setAnimationRunningAt(world, false);
        this.cancel();
    }
}
