package me.mrgeneralq.sleepmost.core.eventlisteners;

import me.mrgeneralq.sleepmost.core.interfaces.IFlagsRepository;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntitySpawnEventListener implements Listener
{
	private final IFlagsRepository flagsRepository;

	public EntitySpawnEventListener(IFlagsRepository flagsRepository)
	{
		this.flagsRepository = flagsRepository;
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e){

		World world = e.getEntity().getWorld();
		
		if(e.getEntityType() != EntityType.PHANTOM)
			return;

		if(!this.flagsRepository.getPreventPhantomFlag().getValueAt(world))
			return;

		e.setCancelled(true);
	}

}
