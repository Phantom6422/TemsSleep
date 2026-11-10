package me.mrgeneralq.sleepmost.core.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.mrgeneralq.sleepmost.core.Sleepmost;
import me.mrgeneralq.sleepmost.core.interfaces.IFlagsRepository;
import me.mrgeneralq.sleepmost.core.interfaces.ISleepService;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PapiExtension extends PlaceholderExpansion {

    private final Sleepmost plugin;
    private final IFlagsRepository flagsRepository;
    private final ISleepService sleepService;
    private final String identifier;

    public PapiExtension(Sleepmost sleepmost, IFlagsRepository flagsRepository, ISleepService sleepService, String identifier) {
        this.plugin = sleepmost;
        this.flagsRepository = flagsRepository;
        this.sleepService = sleepService;
        this.identifier = identifier;
    }
    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getAuthor() {
        return this.plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {

        if (player == null) {
            return "";
        }

        Player onlinePlayer = player.getPlayer();
        // PAPI may request placeholders for offline players (e.g. in menus).
        if (onlinePlayer == null || !onlinePlayer.isOnline()) {
            return "";
        }

        //disable placeholders for non enabled worlds
        if(!sleepService.isEnabledAt(onlinePlayer.getWorld()))
            return "";

        if(identifier.endsWith("_flag")){

            String flagName = identifier.substring(0, identifier.length() - "_flag".length());

           if(!this.flagsRepository.flagExists(flagName))
                return "";

           return String.valueOf(flagsRepository.getFlag(flagName).getValueAt(onlinePlayer.getWorld()));

        }

        if(identifier.equalsIgnoreCase("sleeping-percentage")){
            return String.valueOf(this.sleepService.getSleepersPercentage(onlinePlayer.getWorld()));
        }

        if(identifier.equalsIgnoreCase("sleeping-count")){
            return String.valueOf(this.sleepService.getSleepersAmount(onlinePlayer.getWorld()));
        }

        if(identifier.equalsIgnoreCase("players-required")){
            return String.valueOf(this.sleepService.getRequiredSleepersCount(onlinePlayer.getWorld()));
        }

        if(identifier.equalsIgnoreCase("sleeping")){
            return String.valueOf(this.sleepService.isPlayerAsleep(onlinePlayer));
        }

        if(identifier.equalsIgnoreCase("remaining-count")){
            return String.valueOf(this.sleepService.getRemainingSleepers(onlinePlayer.getWorld()));
        }

        return "";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }


}
