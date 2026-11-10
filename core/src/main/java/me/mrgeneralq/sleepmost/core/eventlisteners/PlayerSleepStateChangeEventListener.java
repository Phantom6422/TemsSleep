package me.mrgeneralq.sleepmost.core.eventlisteners;

import me.mrgeneralq.sleepmost.core.Sleepmost;
import me.mrgeneralq.sleepmost.core.enums.MessageKey;
import me.mrgeneralq.sleepmost.core.enums.SleepSkipCause;
import me.mrgeneralq.sleepmost.core.enums.SleepState;
import me.mrgeneralq.sleepmost.core.events.PlayerSleepStateChangeEvent;
import me.mrgeneralq.sleepmost.core.builders.MessageBuilder;
import me.mrgeneralq.sleepmost.core.interfaces.*;
import me.mrgeneralq.sleepmost.core.models.SleepMostWorld;
import me.mrgeneralq.sleepmost.core.statics.DataContainer;
import me.mrgeneralq.sleepmost.core.statics.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerSleepStateChangeEventListener implements Listener {

    private final Sleepmost sleepmost;
    private final ISleepService sleepService;
    private final IFlagsRepository flagsRepository;
    private final IBossBarService bossBarService;
    private final IMessageService messageService;
    private final ICooldownService cooldownService;
    private final ISleepMostWorldService sleepMostWorldService;

    public PlayerSleepStateChangeEventListener(
            Sleepmost sleepmost,
            ISleepService sleepService,
            IFlagsRepository flagsRepository,
            IBossBarService bossBarService,
            IMessageService messageService,
            ICooldownService cooldownService,
            ISleepMostWorldService sleepMostWorldService
    ) {
        this.sleepmost = sleepmost;
        this.sleepService = sleepService;
        this.flagsRepository = flagsRepository;
        this.bossBarService = bossBarService;
        this.messageService = messageService;
        this.cooldownService = cooldownService;
        this.sleepMostWorldService = sleepMostWorldService;
    }

    @EventHandler
    public void onPlayerSleepStateChange(PlayerSleepStateChangeEvent e) {

        SleepState sleepState = e.getSleepState();
        Player player = e.getPlayer();
        World world = player.getWorld();

        /*
         * Handle some simple logic when players wake up
         */

        //do nothing if there is already running an animation
        if (DataContainer.getContainer().isAnimationRunningAt(world))
            return;


        /*
         * When the player is waking up, we always want to update the bossbar. Also if the world is getting disabled!
         */
        if (sleepState == SleepState.AWAKE) {
            if (ServerVersion.CURRENT_VERSION.supportsBossBars() && this.flagsRepository.getUseBossBarFlag().getValueAt(world))
                this.updateBossBar(world);
            return;
        }

        /*
         * All below code runs when a player went to Sleep
         */
        if (!sleepService.isEnabledAt(world)) {
            return;
        }

        if (ServerVersion.CURRENT_VERSION.supportsBossBars() && this.flagsRepository.getUseBossBarFlag().getValueAt(world))
            this.updateBossBar(world);

        SleepSkipCause skipCause = this.sleepService.getCurrentSkipCause(world);
        int skipDelay = Math.max(this.flagsRepository.getSkipDelayFlag().getValueAt(world), 0);

        int playersSleepingAmount = this.sleepService.getSleepersAmount(world);
        int playersRequiredAmount = this.sleepService.getRequiredSleepersCount(world);

        if (!this.cooldownService.cooldownEnabled() || !this.cooldownService.isCoolingDown(player)) {
            this.messageService.sendPlayerLeftMessage(player, skipCause, playersSleepingAmount, playersRequiredAmount);
            this.cooldownService.startCooldown(player);
        }

        if (playersSleepingAmount < playersRequiredAmount)
            return;

        Bukkit.getScheduler().runTaskLater(sleepmost, () ->
        {

            SleepMostWorld sleepMostWorld = this.sleepMostWorldService.getWorld(world);
            if (sleepMostWorld == null) {
                return;
            }

            //if animation is already running, cancel
            if (sleepMostWorld.isTimeCycleAnimationIsRunning())
                return;

            //final check before night skip is required
            if (!this.sleepService.shouldSkip(world)) {
                return;
            }
            //if animation is enabled, run animation instead
            if (this.flagsRepository.getNightcycleAnimationFlag().getValueAt(world)) {
                this.sleepService.runSkipAnimation(player, skipCause);
                return;
            }

            //retrieve a list of all players currently asleep and send them to the SleepSkipEvent (in service)
            List<OfflinePlayer> peopleWhoSlept = this.sleepService.getSleepers(world).stream().map(p -> Bukkit.getOfflinePlayer(p.getUniqueId())).collect(Collectors.toList());
            this.sleepService.executeSleepReset(world, player.getName(), player.getDisplayName(), peopleWhoSlept, skipCause);
        }, skipDelay * 20L);

    }

    private void updateBossBar(World world) {

        BossBar bossBar = this.bossBarService.getBossBar(world);
        if (bossBar == null) {
            return;
        }

        SleepSkipCause cause = this.sleepService.getCurrentSkipCause(world);

        int playersRequiredAmount = this.sleepService.getRequiredSleepersCount(world);
        int sleepingPlayersAmount = this.sleepService.getSleepersAmount(world);

        String configBossBarTitle = this.messageService.getMessage(MessageKey.BOSS_BAR_TITLE)
                .setWorld(world)
                .build();
        String bossBarTitle = new MessageBuilder(configBossBarTitle, "")
                .usePrefix(false)
                .setSleepingCount(Math.min(sleepingPlayersAmount, playersRequiredAmount))
                .setSleepingRequiredCount(playersRequiredAmount)
                .setCause(cause)
                .build();

        bossBar.setTitle(bossBarTitle);
        bossBar.setProgress(playersRequiredAmount <= 0
                ? 0D
                : Math.min((double) sleepingPlayersAmount / (double) playersRequiredAmount, 1D));
        bossBar.setVisible(sleepingPlayersAmount > 0);
    }

}
