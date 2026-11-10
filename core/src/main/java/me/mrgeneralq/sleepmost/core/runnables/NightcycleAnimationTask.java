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
    private static final int CLOCK_TITLE_INTERVAL_TICKS = 10;

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

        final boolean night = sleepService.isNight(world);

        //CANCEL iF animation is no longer supposed to run
        if(!this.flagsRepository.getForceNightcycleAnimationFlag().getValueAt(world) && night){
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
                sendTitles(0);
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
                p.sendTitle(clockTitle, clockSubTitle, 0, 70, 20);
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
