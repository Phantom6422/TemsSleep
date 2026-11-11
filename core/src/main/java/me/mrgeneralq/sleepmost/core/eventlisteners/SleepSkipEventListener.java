package me.mrgeneralq.sleepmost.core.eventlisteners;

import static me.mrgeneralq.sleepmost.core.enums.SleepSkipCause.NIGHT_TIME;

import me.mrgeneralq.sleepmost.core.enums.MessageKey;
import me.mrgeneralq.sleepmost.core.enums.SleepMostHook;
import me.mrgeneralq.sleepmost.core.enums.SleepersOrAllType;
import me.mrgeneralq.sleepmost.core.Sleepmost;
import me.mrgeneralq.sleepmost.core.flags.SkipSoundFlag;
import me.mrgeneralq.sleepmost.core.flags.UseSkipSoundFlag;
import me.mrgeneralq.sleepmost.core.hooks.GsitHook;
import me.mrgeneralq.sleepmost.core.builders.MessageBuilder;
import me.mrgeneralq.sleepmost.core.interfaces.*;
import me.mrgeneralq.sleepmost.core.models.Hook;
import me.mrgeneralq.sleepmost.core.statics.ServerVersion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.mrgeneralq.sleepmost.core.enums.SleepSkipCause;
import me.mrgeneralq.sleepmost.core.events.SleepSkipEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SleepSkipEventListener implements Listener {

    private final IMessageService messageService;
    private final IConfigService configService;
    private final ISleepService sleepService;
    private final IFlagsRepository flagsRepository;
    private final IBossBarService bossBarService;
    private final IHookService hookService;

    public SleepSkipEventListener(IMessageService messageService,
                                  IConfigService configService,
                                  ISleepService sleepService,
                                  IFlagsRepository flagsRepository,
                                  IBossBarService bossBarService,
                                  IHookService hooksService
    ) {

        this.messageService = messageService;
        this.configService = configService;
        this.sleepService = sleepService;
        this.flagsRepository = flagsRepository;
        this.bossBarService = bossBarService;
        this.hookService = hooksService;
    }

    @EventHandler
    public void onSleepSkip(SleepSkipEvent e) {
        World world = e.getWorld();
        SleepSkipCause cause = e.getCause();

        // A third-party plugin can fire this event with any cause. Only night
        // and storm skips are meaningful to SleepMost.
        if (cause != NIGHT_TIME && cause != SleepSkipCause.STORM) {
            return;
        }

        /*
        * Decide which players should be phantom reset when the night skips
        */
        SleepersOrAllType resetAudience = this.flagsRepository.getPhantomResetAudienceFlag().getValueAt(world);

        List<OfflinePlayer> playersToResetPhantom;
        if(resetAudience == SleepersOrAllType.SLEEPERS)
            playersToResetPhantom = e.getPeopleWhoSlept();
        else
            playersToResetPhantom = e.getWorld().getPlayers().stream().map(p -> Bukkit.getOfflinePlayer(p.getUniqueId())).collect(Collectors.toList());


        if(this.flagsRepository.getResetTimeSinceRestFlag().getValueAt(world)){
            resetPhantomCounter(world, playersToResetPhantom);
        }

        sendSkipSound(world, e, cause);

        if (ServerVersion.CURRENT_VERSION.supportsTitles()) {
            Sleepmost plugin = Sleepmost.getInstance();
            if (plugin != null && plugin.isEnabled()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> sendSkipTitle(world, e), 5L);
            }
        }

        boolean shouldHeal = flagsRepository.getHealFlag().getValueAt(world);
        boolean shouldFeed = flagsRepository.getFeedFlag().getValueAt(world);

        List<OfflinePlayer> sleepingPlayers = e.getPeopleWhoSlept();

        sleepingPlayers.forEach(p -> {

            if(p.isOnline()){
                Player onlinePlayer = p.getPlayer();
                if (onlinePlayer == null) {
                    return;
                }
                if (shouldHeal)
                    ServerVersion.CURRENT_VERSION.healToMaxHP(onlinePlayer);

                if (shouldFeed)
                    onlinePlayer.setFoodLevel(20);
            }

        });

        List<OfflinePlayer> playersWhoSlept = e.getPeopleWhoSlept();

        //if the flag is set to "sleepers" only, we only send the message to the players who slept
        if(this.flagsRepository.getSkipMsgAudienceFlag().getValueAt(world) == SleepersOrAllType.SLEEPERS){
            this.messageService.sendNightSkippedMessage(playersWhoSlept, world, e.getLastSleeperName(), e.getLastSleeperDisplayName(), cause);
        }else{
            this.messageService.sendNightSkippedMessage(world, e.getLastSleeperName(), e.getLastSleeperDisplayName(), cause);
        }

        this.sleepService.clearSleepersAt(world);

        if(ServerVersion.CURRENT_VERSION.supportsBossBars() && this.bossBarService != null){
            this.bossBarService.setVisible(world, false);
        }


        Optional<Hook> optionalGsitHook = this.hookService.getHook(SleepMostHook.GSIT);
        //GSit, disable sleep
        if (optionalGsitHook.isPresent() && this.flagsRepository.getGSitHookFlag().getValueAt(world) && this.flagsRepository.getGSitSleepFlag().getValueAt(world)) {
            GsitHook gsitHook = (GsitHook) optionalGsitHook.get();

            for (OfflinePlayer player : playersWhoSlept) {
                if (player.isOnline()) {
                    Player onlinePlayer = player.getPlayer();
                    if (onlinePlayer != null) {
                        gsitHook.setSleepingPose(onlinePlayer, false);
                    }
                }
            }
        }
    }

    private void resetPhantomCounter(World world, List<OfflinePlayer> playersWhoSlept) {
        /*
         * DISCLAIMER: Statistic and TIME_SINCE_REST Does not exist
         * in older versions of Minecraft
         */
        try {
            for (Player p : playersWhoSlept.stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).filter(java.util.Objects::nonNull).collect(Collectors.toList()))
                p.setStatistic(Statistic.TIME_SINCE_REST, 0);
        } catch (NoSuchFieldError | IllegalArgumentException error) {
            // statistic did not exist yet in some versions
        }
    }

    private void sendSkipTitle(World world , SleepSkipEvent e) {

        SleepSkipCause cause = e.getCause();

        boolean titleEnabled = (cause == NIGHT_TIME ? this.flagsRepository.getUseTitleNightSkippedFlag().getValueAt(world) :
                this.flagsRepository.getUseTitleStormSkippedFlag().getValueAt(world));

        if (!titleEnabled) {
            return;
        }

        String nightSkippedTitle = messageService.getMessage(MessageKey.NIGHT_SKIPPED_TITLE).setWorld(world).build();
        String nightSkippedSubtitle = messageService.getMessage(MessageKey.NIGHT_SKIPPED_SUBTITLE).setWorld(world).build();
        String stormSkippedTitle = messageService.getMessage(MessageKey.STORM_SKIPPED_TITLE).setWorld(world).build();
        String stormSkippedSubtitle = messageService.getMessage(MessageKey.STORM_SKIPPED_SUBTITLE).setWorld(world).build();


        String skippedTitle = (cause == NIGHT_TIME ? nightSkippedTitle : stormSkippedTitle);
        String skippedSubtitle = (cause == NIGHT_TIME ? nightSkippedSubtitle : stormSkippedSubtitle);

        List<Player> playerList = (flagsRepository.getNonSleepingTitleFlag().getValueAt(world) ?
                world.getPlayers():
                e.getPeopleWhoSlept().stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).filter(java.util.Objects::nonNull).collect(Collectors.toList()));

        for (Player p : playerList) {

            MessageBuilder titleMessageBuilder = this.messageService.getMessage(skippedTitle)
                    .setPlayer(p)
                    .usePrefix(false)
                    .setWorld(world);

            MessageBuilder subTitleMessageBuilder = this.messageService.getMessage(skippedSubtitle)
                    .setPlayer(p)
                    .usePrefix(false)
                    .setWorld(world);

            String playerTitle = titleMessageBuilder.build();
            String playerSubTitle = subTitleMessageBuilder.build();

            p.sendTitle(playerTitle, playerSubTitle, 10, 70, 20);
        }
    }

    private void sendSkipSound(World world, SleepSkipEvent e, SleepSkipCause cause) {
        UseSkipSoundFlag enabledFlag = cause == NIGHT_TIME
                ? flagsRepository.getUseSoundNightSkippedFlag()
                : flagsRepository.getUseSoundStormSkippedFlag();

        if (!enabledFlag.getValueAt(world)) return;

        SkipSoundFlag skipSoundFlag = cause == NIGHT_TIME
                ? flagsRepository.getSkipNightSoundFlag()
                : flagsRepository.getSkipStormSoundFlag();
        String skipSound = skipSoundFlag.getValueAt(world);

        List<Player> playerList = (flagsRepository.getNonSleepingSoundFlag().getValueAt(world) ?
                world.getPlayers():
                e.getPeopleWhoSlept().stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).filter(java.util.Objects::nonNull).collect(Collectors.toList()));

        for (Player p : playerList) {
            p.playSound(p.getLocation(), skipSound, 0.4F, 1F);
        }
    }
}
