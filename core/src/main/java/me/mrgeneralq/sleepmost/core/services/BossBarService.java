package me.mrgeneralq.sleepmost.core.services;

import me.mrgeneralq.sleepmost.core.interfaces.IBossBarService;
import me.mrgeneralq.sleepmost.core.repositories.BossBarRepository;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarService implements IBossBarService {

    BossBarRepository bossBarRepository;

    public BossBarService(BossBarRepository bossBarRepository) {
        this.bossBarRepository = bossBarRepository;
    }

    @Override
    public void registerBossBar(World world) {

        BossBar previous = this.bossBarRepository.get(world);
        if (previous != null) {
            previous.removeAll();
        }

        BossBar bossBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
        bossBar.setVisible(false);

        this.bossBarRepository.set(world, bossBar);

    }

    @Override
    public void unregisterBossBar(World world) {

        BossBar bossBar = this.bossBarRepository.get(world);
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }

        this.bossBarRepository.remove(world);

    }

    @Override
    public void registerPlayer(World world, Player player) {

        BossBar bossBar = this.bossBarRepository.get(world);

        if(bossBar == null || player == null)
            return;

        bossBar.addPlayer(player);
    }

    @Override
    public void unregisterPlayer(World world, Player player) {

        BossBar bossBar = this.bossBarRepository.get(world);

        if(bossBar == null || player == null)
            return;

        bossBar.removePlayer(player);
    }

    @Override
    public void unregisterBossBar(World world, Player player) {

        BossBar bossBar = bossBarRepository.get(world);
        if (bossBar != null && player != null) {
            bossBar.removePlayer(player);
        }
        this.bossBarRepository.remove(world);
    }

    @Override
    public void setVisible(World world, boolean visible) {

        BossBar bossBar = this.bossBarRepository.get(world);
        if (bossBar != null) {
            bossBar.setVisible(visible);
        }

    }

    @Override
    public BossBar getBossBar(World world) {

        return this.bossBarRepository.get(world);
    }
}
