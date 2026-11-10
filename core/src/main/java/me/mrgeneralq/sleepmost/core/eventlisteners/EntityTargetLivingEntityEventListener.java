package me.mrgeneralq.sleepmost.core.eventlisteners;

import me.mrgeneralq.sleepmost.core.interfaces.IFlagsRepository;
import me.mrgeneralq.sleepmost.core.interfaces.ISleepService;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EntityTargetLivingEntityEventListener implements Listener{

	private final IFlagsRepository flagsRepository;
	private final ISleepService sleepService;

	public EntityTargetLivingEntityEventListener(IFlagsRepository flagsRepository, ISleepService sleepService) {
		this.flagsRepository = flagsRepository;
		this.sleepService = sleepService;
	}

	@EventHandler
	public void onMobTarget(EntityTargetLivingEntityEvent event) {
		if(!(event.getTarget() instanceof Player))
			return;

		Player player = (Player) event.getTarget();
		World world = player.getWorld();

		// SleepMost also tracks synthetic sleep (/sm sleep and GSit poses), which
		// is not reflected by Player#isSleeping().
		if(!player.isSleeping() && !this.sleepService.isPlayerAsleep(player))
			return;

		if(!this.flagsRepository.getMobNoTargetFlag().getValueAt(world))
			return;

		event.setCancelled(true);
	}
}
