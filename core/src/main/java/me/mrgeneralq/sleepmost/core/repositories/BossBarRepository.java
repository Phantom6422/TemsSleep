package me.mrgeneralq.sleepmost.core.repositories;

import me.mrgeneralq.sleepmost.core.interfaces.IRepository;
import org.bukkit.World;
import org.bukkit.boss.BossBar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarRepository implements IRepository<World, BossBar> {

    private final Map<UUID, BossBar> bossBarList = new HashMap<>();

    @Override
    public BossBar get(World world) {
        return world == null ? null : this.bossBarList.get(world.getUID());
    }

    @Override
    public void set(World world, BossBar bossBar) {
        if (world != null) {
            this.bossBarList.put(world.getUID(), bossBar);
        }
    }

    @Override
    public boolean exists(World world) {
        return world != null && this.bossBarList.containsKey(world.getUID());
    }

    @Override
    public void remove(World world) {
        if (world != null) {
            this.bossBarList.remove(world.getUID());
        }
    }

    public Collection<BossBar> getAll() {
        return this.bossBarList.values();
    }

    public void clear() {
        this.bossBarList.clear();
    }
}
