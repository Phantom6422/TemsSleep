package me.mrgeneralq.sleepmost.core.runnables;

import me.mrgeneralq.sleepmost.core.enums.SleepSkipCause;
import me.mrgeneralq.sleepmost.core.enums.TimeCycle;
import me.mrgeneralq.sleepmost.core.events.TimeCycleChangeEvent;
import me.mrgeneralq.sleepmost.core.interfaces.IFlagsRepository;

import me.mrgeneralq.sleepmost.core.interfaces.ISleepMostWorldService;
import me.mrgeneralq.sleepmost.core.interfaces.ISleepService;
import me.mrgeneralq.sleepmost.core.models.SleepMostWorld;
import me.mrgeneralq.sleepmost.core.statics.ServerVersion;
import me.mrgeneralq.sleepmost.core.statics.Time;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Calendar;

public class Heartbeat extends BukkitRunnable {


    private final ISleepService sleepService;
    private final ISleepMostWorldService sleepMostWorldService;
    private final IFlagsRepository flagsRepository;

    public Heartbeat(ISleepService sleepService, ISleepMostWorldService sleepMostWorldService, IFlagsRepository flagsRepository) {
        this.sleepService = sleepService;
        this.sleepMostWorldService = sleepMostWorldService;
        this.flagsRepository = flagsRepository;
    }

    @Override
    public void run() {

        for(World world: Bukkit.getWorlds())
        {
            if (!this.sleepService.isEnabledAt(world)) {
                continue;
            }

            SleepMostWorld sleepMostWorld = this.sleepMostWorldService.getWorld(world);
            if (sleepMostWorld == null) {
                // The world was loaded before the plugin registered its state.
                this.sleepMostWorldService.registerWorld(world);
                sleepMostWorld = this.sleepMostWorldService.getWorld(world);
                if (sleepMostWorld == null) {
                    continue;
                }
            }

            updateTimeCycle(world, sleepMostWorld);

            if(ServerVersion.CURRENT_VERSION.supportsGameRules()){
                checkPlannedFreezeRequired(world, sleepMostWorld);
                checkFreezeRequired(world, sleepMostWorld);
                checkUnfreezeRequired(world, sleepMostWorld);
            }

            SleepSkipCause cause = this.sleepService.getCurrentSkipCause(world);
            if(cause == SleepSkipCause.UNKNOWN){
                for(Player p: new ArrayList<>(world.getPlayers())){
                    if (this.sleepService.isPlayerAsleep(p)) {
                        this.sleepService.setSleeping(p, false);
                    }
                }
            }
        }
    }

    private void updateTimeCycle(World world, SleepMostWorld sleepMostWorld){

        TimeCycle newTimeCycle = this.sleepService.isNight(world) ? TimeCycle.NIGHT : TimeCycle.DAY;

        //prevent event from being fired unless TimeCycle is not unknown
        if(sleepMostWorld.getTimeCycle() == TimeCycle.UNKNOWN){
            sleepMostWorld.setTimeCycle(newTimeCycle);
            this.sleepMostWorldService.updateWorld(sleepMostWorld);
            return;
        }

        //only run if time cycle changes
        if(sleepMostWorld.getTimeCycle() == newTimeCycle)
            return;

        sleepMostWorld.setTimeCycle(newTimeCycle);
        this.sleepMostWorldService.updateWorld(sleepMostWorld);
        Bukkit.getPluginManager().callEvent(new TimeCycleChangeEvent(world, newTimeCycle));
    }

    private void checkFreezeRequired(World world, SleepMostWorld sleepMostWorld){

        double time = world.getTime();

        if(sleepMostWorld.isFrozen())
            return;

        if(!sleepMostWorld.isPlannedFrozen())
           return;

        if(!Time.aroundMidNight(time))
            return;

        int longerNightsDuration = this.flagsRepository.getLongerNightDurationFlag().getValueAt(world);

        if(longerNightsDuration <= 0)
            return;

        Calendar timeToResume = Calendar.getInstance();
        timeToResume.add(Calendar.SECOND, longerNightsDuration);
        this.sleepMostWorldService.freezeTime(world, timeToResume);
    }

    private void checkUnfreezeRequired(World world, SleepMostWorld sleepMostWorld){

        if(!sleepMostWorld.isFrozen())
            return;

        Calendar frozenUntil = sleepMostWorld.getFrozenUntil();
        if(frozenUntil != null
                && frozenUntil.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()
                && !sleepMostWorld.isTimeCycleAnimationIsRunning())
            return;

        this.sleepMostWorldService.unfreezeTime(world);
    }

    /*
     * Check if the freeze needs to be scheduled
     */
    private void checkPlannedFreezeRequired(World world, SleepMostWorld sleepMostWorld){

        double time = world.getTime();
        if(!Time.aroundTime(Time.MID_NIGHT - 500, time))
            return;

        double duration = this.flagsRepository.getLongerNightDurationFlag().getValueAt(world);

        if(duration <= 0)
            return;
        sleepMostWorld.setPlannedFrozen(true);
    }

}
