package me.mrgeneralq.sleepmost.core.eventlisteners;

import me.mrgeneralq.sleepmost.core.enums.MessageKey;
import me.mrgeneralq.sleepmost.core.interfaces.*;
import me.mrgeneralq.sleepmost.core.models.SleepMostWorld;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import io.papermc.paper.block.bed.BedEnterAction;
import io.papermc.paper.block.bed.BedEnterProblem;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class PlayerBedEnterEventListener implements Listener {

    private final ISleepService sleepService;
    private final IMessageService messageService;
    private final IFlagsRepository flagsRepository;
    private final ISleepMostWorldService sleepMostWorldService;
    private final IInsomniaService insomniaService;

    public PlayerBedEnterEventListener(ISleepService sleepService,
                                       IMessageService messageService,
                                       ICooldownService cooldownService,
                                       IFlagsRepository flagsRepository,
                                       IBossBarService bossBarService,
                                       ISleepMostWorldService sleepMostWorldService,
                                       IInsomniaService insomniaService
    ) {
        this.sleepService = sleepService;
        this.messageService = messageService;
        this.flagsRepository = flagsRepository;
        this.sleepMostWorldService = sleepMostWorldService;
        this.insomniaService = insomniaService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBedEnter(PlayerBedEnterEvent e) {

        Player player = e.getPlayer();
        World world = player.getWorld();

        if (!this.sleepService.isEnabledAt(world)) {
            return;
        }

        /*
         * A bed interaction can be a sleep attempt or only a respawn-point
         * update (for example during the day). Do not cancel the latter:
         * cancelling PlayerBedEnterEvent prevents the whole bed interaction,
         * including setting the player's spawn.
         */
        BedEnterAction action = e.enterAction();
        if (e.isCancelled()) {
            if (this.sleepService.isPlayerAsleep(player)) {
                this.sleepService.setSleeping(player, false);
            }
            return;
        }
        if (action.problem() != null) {
            this.sendCannotSleepMessage(player, world, action.problem());
            if (this.sleepService.isPlayerAsleep(player)) {
                this.sleepService.setSleeping(player, false);
            }
            return;
        }
        if (!action.canSleep().success()) {
            // Daytime or another non-sleepable situation. Vanilla may still
            // allow setting the respawn point, so leave the event alone and
            // never count the player as sleeping.
            if (this.sleepService.isPlayerAsleep(player)) {
                this.sleepService.setSleeping(player, false);
            }
            return;
        }

        if (!this.sleepService.isSleepingPossible(world)) {
            this.messageService.sendMessage(player, this.messageService.getMessagePrefixed(MessageKey.CANNOT_SLEEP_NOW)
                    .setWorld(world)
                    .setPlayer(player)
                    .build());
            e.setCancelled(true);
            return;
        }

        //check if sleep is allowed in world
        if(this.flagsRepository.getPreventSleepFlag().getValueAt(world)) {
            String sleepPreventedConfigMessage = messageService.getMessagePrefixed(MessageKey.SLEEP_PREVENTED)
                    .setPlayer(player)
                    .setWorld(world)
                    .build();

            this.messageService.sendMessage(player,sleepPreventedConfigMessage);
            e.setCancelled(true);
            return;
        }

        if (world.isThundering() && !this.flagsRepository.getStormSleepFlag().getValueAt(world)) {

            this.messageService.sendMessage(player, messageService.getMessagePrefixed(MessageKey.NO_SLEEP_THUNDERSTORM)
                    .setPlayer(player)
                    .setWorld(world)
                    .build());

            e.setCancelled(true);
            return;
        }

        if(this.insomniaService.hasInsomniaEnabled(player) && this.flagsRepository.getInsomniaChanceFlag().getValueAt(world) > 0){
            String insomniaMessage = this.messageService.getMessagePrefixed(MessageKey.INSOMNIA_NOT_SLEEPY)
                    .setWorld(world)
                    .setPlayer(player)
                    .build();
            this.messageService.sendMessage(player,insomniaMessage);
            e.setCancelled(true);
            return;
        }

        SleepMostWorld sleepMostWorld = this.sleepMostWorldService.getWorld(world);

        if(sleepMostWorld != null && sleepMostWorld.isFrozen()){

            String longerNightsSleepPreventedMsg = this.messageService.getMessagePrefixed(MessageKey.SLEEP_PREVENTED_LONGER_NIGHT)
                    .setWorld(world)
                    .setPlayer(player)
                    .build();
            this.messageService.sendMessage(player, longerNightsSleepPreventedMsg);
            e.setCancelled(true);
            return;
        }

        if(!this.sleepService.isPlayerAsleep(player))
        this.sleepService.setSleeping(player , true);
    }

    private void sendCannotSleepMessage(Player player, World world, BedEnterProblem problem) {
        MessageKey key = problem == BedEnterProblem.NOT_SAFE
                ? MessageKey.SLEEP_UNSAFE
                : MessageKey.CANNOT_SLEEP_NOW;

        this.messageService.sendMessage(player, this.messageService.getMessagePrefixed(key)
                .setWorld(world)
                .setPlayer(player)
                .build());
    }
}
