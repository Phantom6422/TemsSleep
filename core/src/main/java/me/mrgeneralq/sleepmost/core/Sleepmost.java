package me.mrgeneralq.sleepmost.core;

import me.mrgeneralq.sleepmost.core.enums.SleepMostHook;
import me.mrgeneralq.sleepmost.core.eventlisteners.*;
import me.mrgeneralq.sleepmost.core.eventlisteners.hooks.GSitEventListener;
import me.mrgeneralq.sleepmost.core.hooks.EssentialsHook;
import me.mrgeneralq.sleepmost.core.hooks.GsitHook;
import me.mrgeneralq.sleepmost.core.hooks.PlaceholderAPIHook;
import me.mrgeneralq.sleepmost.core.hooks.SuperVanishHook;
import me.mrgeneralq.sleepmost.core.interfaces.*;
import me.mrgeneralq.sleepmost.core.mappers.ConfigMessageMapper;
import me.mrgeneralq.sleepmost.core.mappers.MessageMapper;
import me.mrgeneralq.sleepmost.core.models.Hook;
import me.mrgeneralq.sleepmost.core.placeholderapi.PapiExtension;
import me.mrgeneralq.sleepmost.core.providers.GameRuleProvider;
import me.mrgeneralq.sleepmost.core.runnables.Heartbeat;
import me.mrgeneralq.sleepmost.core.statics.DataContainer;
import me.mrgeneralq.sleepmost.core.statics.ServerVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.mrgeneralq.sleepmost.core.commands.SleepmostCommand;
import me.mrgeneralq.sleepmost.core.statics.Bootstrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public class Sleepmost extends JavaPlugin {

	private static final String COMMAND_NAME = "temssleep";
	private static final List<String> COMMAND_ALIASES = List.of("ts", "sleepmost", "sm");

	private static Sleepmost instance;
	private Bootstrapper bootstrapper;
	private Heartbeat heartbeat;
	private final List<PapiExtension> papiExtensions = new ArrayList<>();
	private final List<IPlatformModule> platformModules = new ArrayList<>();

	private IMessageService messageService;

	@Override
	public void onEnable() {

		//used to load all modules
		for(IPlatformModule module : ServiceLoader.load(IPlatformModule.class, this.getClass().getClassLoader())){
			module.setup();
			this.platformModules.add(module);
			getLogger().info("Loaded module: " + module.getClass().getName());
		}

		instance = this;
		getLogger().info("Detected Minecraft " + ServerVersion.CURRENT_VERSION.getName()
				+ " (titles=" + ServerVersion.CURRENT_VERSION.supportsTitles()
				+ ", bossbars=" + ServerVersion.CURRENT_VERSION.supportsBossBars()
				+ ", gamerules=" + ServerVersion.CURRENT_VERSION.supportsGameRules() + ")");
		saveDefaultConfig();

		//init metrics
		final int bStatsID = 6212;
		new Metrics(this, bStatsID);

		//load the messages at start
		MessageMapper.getMapper().loadMessages();

		//init bootstrapper
		this.bootstrapper = Bootstrapper.getBootstrapper();
		bootstrapper.initialize(this, Bukkit.getPluginManager());

		this.messageService = bootstrapper.getMessageService();

		//creating missing messages
		this.messageService.createMissingMessages();

		//check if boss bars are supported before registered
		if(ServerVersion.CURRENT_VERSION.supportsBossBars())
			this.registerBossBars();

		//init commands
		SleepmostCommand sleepmostCommand = new SleepmostCommand(bootstrapper.getSleepService(), bootstrapper.getMessageService(), bootstrapper.getUpdateService(), bootstrapper.getFlagService(), bootstrapper.getFlagsRepository(), bootstrapper.getConfigRepository(), bootstrapper.getCooldownService(), bootstrapper.getBossBarService(), bootstrapper.getSleepMostWorldService(), bootstrapper.getSleepMostPlayerService(), bootstrapper.getInsomniaService(), bootstrapper.getDebugService(), bootstrapper.getHookService());
		this.registerSleepmostCommand(sleepmostCommand);

		PluginManager pm = Bukkit.getPluginManager();

		pm.registerEvents(new PlayerBedEnterEventListener(bootstrapper.getSleepService(), bootstrapper.getMessageService(), bootstrapper.getCooldownService(), bootstrapper.getFlagsRepository(), bootstrapper.getBossBarService(), bootstrapper.getSleepMostWorldService(), bootstrapper.getInsomniaService()), this);
		pm.registerEvents(new PlayerQuitEventListener(bootstrapper.getCooldownService(), bootstrapper.getSleepService(), bootstrapper.getBossBarService(), bootstrapper.getSleepMostPlayerService()), this);
		pm.registerEvents(new EntityTargetLivingEntityEventListener(bootstrapper.getFlagsRepository(), bootstrapper.getSleepService()), this);
		pm.registerEvents(new PlayerWorldChangeEventListener(bootstrapper.getSleepService(), bootstrapper.getMessageService(), bootstrapper.getBossBarService()), this);
		pm.registerEvents(new PlayerJoinEventListener(this, bootstrapper.getUpdateService(), bootstrapper.getMessageService(), bootstrapper.getBossBarService(), bootstrapper.getSleepMostPlayerService()), this);
		pm.registerEvents(new EntitySpawnEventListener(bootstrapper.getFlagsRepository()), this);

		if(ServerVersion.CURRENT_VERSION.hasTimeSkipEvent()){
			pm.registerEvents(new TimeSkipEventListener(bootstrapper.getSleepService()), this);
		}

		pm.registerEvents(new SleepSkipEventListener(bootstrapper.getMessageService(), bootstrapper.getConfigService(), bootstrapper.getSleepService(), bootstrapper.getFlagsRepository(), bootstrapper.getBossBarService(), bootstrapper.getHookService()), this);
		pm.registerEvents(new PlayerBedLeaveEventListener(bootstrapper.getSleepService()), this);
		pm.registerEvents(new WorldLoadEventListener(bootstrapper.getBossBarService(), bootstrapper.getSleepMostWorldService()),this);
		pm.registerEvents(new WorldUnloadEventListener(bootstrapper.getBossBarService(), bootstrapper.getSleepMostWorldService()),this);
		pm.registerEvents(new PlayerSleepStateChangeEventListener(this, bootstrapper.getSleepService(), bootstrapper.getFlagsRepository(), bootstrapper.getBossBarService(), bootstrapper.getMessageService(), bootstrapper.getCooldownService(), bootstrapper.getSleepMostWorldService()), this);
		pm.registerEvents(new TimeCycleChangeEventListener(bootstrapper.getSleepService(), bootstrapper.getSleepMostWorldService(), bootstrapper.getFlagsRepository(), bootstrapper.getInsomniaService()),this );
		pm.registerEvents(new PlayerConsumeEventListener(bootstrapper.getSleepService(), bootstrapper.getInsomniaService(), bootstrapper.getMessageService(), bootstrapper.getFlagsRepository()), this);

		//REGISTER HOOKS
		registerHooks();

		Optional<Hook> gsitHook = bootstrapper.getHookService().getHook(SleepMostHook.GSIT);
		if(gsitHook.isPresent()){
			pm.registerEvents(new GSitEventListener(bootstrapper.getSleepService(), bootstrapper.getMessageService(), bootstrapper.getCooldownService(), bootstrapper.getFlagsRepository(), bootstrapper.getBossBarService(), bootstrapper.getSleepMostWorldService(), bootstrapper.getInsomniaService()),this);
			getLogger().info("GSit hook detected and registered GSit event listener");
		}

		Optional<Hook> placeholderAPIHook = bootstrapper.getHookService().getHook(SleepMostHook.PLACEHOLDER_API);
		if(placeholderAPIHook.isPresent()){
			// Keep the legacy identifier working for existing placeholders.
			for (String identifier : List.of("temssleep", "sleepmost")) {
				PapiExtension extension = new PapiExtension(this, bootstrapper.getFlagsRepository(), bootstrapper.getSleepService(), identifier);
				extension.register();
				this.papiExtensions.add(extension);
			}
		}

		runPlayerTasks();
		runPreTimerTasks();
		runTimers(bootstrapper.getSleepService(), bootstrapper.getSleepMostWorldService());

		Bukkit.getScheduler().runTaskAsynchronously(this, () -> notifyIfNewUpdateExists(bootstrapper.getUpdateService()));
	}

	@Override
	public void onDisable() {
		// Cancel tasks before dropping the services they hold on to.
		Bukkit.getScheduler().cancelTasks(this);
		if (this.heartbeat != null) {
			this.heartbeat.cancel();
			this.heartbeat = null;
		}

		for (PapiExtension extension : this.papiExtensions) {
			try {
				extension.unregister();
			} catch (RuntimeException ignored) {
				// PlaceholderAPI may already be disabled during server shutdown.
			}
		}
		this.papiExtensions.clear();

		IBossBarService bossBarService = this.bootstrapper == null ? null : this.bootstrapper.getBossBarService();
		for (World world : Bukkit.getWorlds()) {
			if (bossBarService != null) {
				bossBarService.unregisterBossBar(world);
			}
			DataContainer.getContainer().clearSleepingPlayers(world);
			DataContainer.getContainer().setAnimationRunningAt(world, false);
		}

		// Static holders keep the plugin class loader (and all worlds/players it
		// references) alive across /reload. Clear them explicitly.
		DataContainer.reset();
		MessageMapper.reset();
		ConfigMessageMapper.reset();
		GameRuleProvider.reset();
		Bootstrapper.reset();
		this.bootstrapper = null;
		this.messageService = null;
		this.platformModules.clear();
		instance = null;
	}

	public static Sleepmost getInstance() {
		return instance;
	}

	private void registerSleepmostCommand(SleepmostCommand sleepmostCommand) {
		// Spigot/Bukkit register the command from plugin.yml.
		try {
			PluginCommand pluginCommand = getCommand(COMMAND_NAME);
			if (pluginCommand != null) {
				pluginCommand.setExecutor(sleepmostCommand);
				pluginCommand.setTabCompleter(sleepmostCommand);
				return;
			}
		} catch (UnsupportedOperationException ignored) {
			// Paper plugins reject getCommand and use the command map below.
		}

		for (IPlatformModule module : this.platformModules) {
			module.registerCommand(this, COMMAND_NAME, COMMAND_ALIASES, sleepmostCommand, sleepmostCommand);
		}
	}

	private void runPreTimerTasks(){
		for(World world: Bukkit.getWorlds()){
			this.bootstrapper.getSleepMostWorldService().registerWorld(world);

			if(ServerVersion.CURRENT_VERSION.supportsGameRules() && this.bootstrapper.getSleepService().isEnabledAt(world)){
				if(this.bootstrapper.getFlagsRepository().getDisableDaylightcycleGamerule().getValueAt(world)){
					IGameRuleService gameRuleService = bootstrapper.getGameRuleService();
					gameRuleService.setAdvanceTime(world,true);
				}
			}
		}
	}

	private void runPlayerTasks(){
		for(Player p: Bukkit.getOnlinePlayers()){
			this.bootstrapper.getSleepMostPlayerService().registerNewPlayer(p);
		}
	}

	private void runTimers(ISleepService sleepService, ISleepMostWorldService sleepMostWorldService){
		this.heartbeat = new Heartbeat(sleepService, sleepMostWorldService, bootstrapper.getFlagsRepository());
		this.heartbeat.runTaskTimer(this, 20,20);
	}

	private void notifyIfNewUpdateExists(IUpdateService updateService)
	{
		if (!isEnabled()) {
			return;
		}
		try {
			if(updateService.hasUpdate()) {
				getLogger().info("UPDATE FOUND: A newer version of TemsSleep is available to download!");
			} else if ("unknown".equals(updateService.getCachedUpdateVersion())) {
				getLogger().warning("Could not read the latest GitHub release. Make sure the update-checker repository is public: " + updateService.getUpdateUrl());
			}
		} catch (RuntimeException exception) {
			getLogger().warning("Could not check for updates: " + exception.getMessage());
		}
	}

	void registerBossBars(){
		for(World world: Bukkit.getWorlds()){
			IBossBarService bossBarService = this.bootstrapper.getBossBarService();
			bossBarService.registerBossBar(world);
			bossBarService.setVisible(world, false);
		}
	}

	void registerHooks(){

		Hook superVanishHook = new SuperVanishHook();
		superVanishHook.addAlias("PremiumVanish");

		IHookService hookService = bootstrapper.getHookService();
		hookService.attemptRegister(superVanishHook);
		hookService.attemptRegister(new PlaceholderAPIHook());
		hookService.attemptRegister(new GsitHook());
		hookService.attemptRegister(new EssentialsHook());


	}
}
