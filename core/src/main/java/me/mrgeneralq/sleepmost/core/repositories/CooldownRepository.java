package me.mrgeneralq.sleepmost.core.repositories;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.mrgeneralq.sleepmost.core.interfaces.ICooldownRepository;
import org.bukkit.entity.Player;

public class CooldownRepository implements ICooldownRepository {

    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    @Override
    public long getPlayerCooldown(Player player) {
        return cooldownMap.getOrDefault(player.getUniqueId(), 0L);
    }

    @Override
    public void setCooldown(Player player) {
        cooldownMap.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public boolean contains(Player player) {
        return this.cooldownMap.containsKey(player.getUniqueId());
    }

    @Override
    public void removeCooldown(Player player) {
        cooldownMap.remove(player.getUniqueId());
    }


}
