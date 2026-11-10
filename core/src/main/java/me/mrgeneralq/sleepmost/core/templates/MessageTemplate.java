package me.mrgeneralq.sleepmost.core.templates;

import me.mrgeneralq.sleepmost.core.enums.MessageKey;

public enum MessageTemplate {
    UNKNOWN_COMMAND("&cUnknown command! Type /ts to get a list of available commands"),
    //ONLY_PLAYERS_COMMAND("&cThat command can only be performed by players."),
    //NO_PERMISSION_COMMAND("&cYou do not have permission to do this"),
    //NO_CONSOLE_COMMAND("&cThis command cannot be executed from the console"),
    //CANNOT_SLEEP_NOW("&cYou cannot sleep right now"),
    //SLEEP_CMD_DISABLED("&cThe sleep-command is disabled"),
    //NO_BED_LOCATION_SET("&cThere is no bed location set"),
    //NO_LONGER_SLEEPING("&cYou are no longer sleeping"),
    //SLEEP_SUCCESS("&bYou are now asleep"),
    //TARGET_NOT_SLEEPING("&cThe player is not sleeping"),
    //TARGET_NOT_ONLINE("&cThe player is not online"),
    //SPECIFY_PLAYER("&cPlease specify with the player username"),
    //ALREADY_ENABLED_FOR_WORLD("&cTemsSleep is already enabled in this world"),
    //ALREADY_DISABLED_FOR_WORLD("&cTemsSleep is already disabled in this world"),
    //ENABLED_FOR_WORLD("&bTemsSleep is now enabled in this world"),
    //DISABLED_FOR_WORLD("&cTemsSleep is now disabled in this world"),
    //CONFIG_RELOADED("&aYou successfully reloaded TemsSleep"),
    CURRENTLY_DISABLED ("&bTemsSleep is currently &cdisabled &bfor this world. Type &a/ts enable &bto enable TemsSleep for this world."),
    //ONE_PLAYER_SLEEP_SET("&bOne player sleep has been configured"),
    //ALL_FLAGS_RESET_SUCCESS("&cAll flags were successfully reset"),
    //INSOMNIA_NOT_SLEEPY("&cYou don`t feel sleepy at the moment"),
    //INSOMNIA_FEELING_SLEEPY("&bYou start feeling sleepy ..."),
    //INSOMNIA_ALREADY_ENABLED("&cInsomnia is already enabled in this world"),
    //INSOMNIA_ENABLED("&bInsomnia is enabled"),
    //CMD_ONLY_DURING_NIGHT("&cThis command can only be used during the night"),
    //KICK_OUT_BED("&c&l[kick out]"),
    //KICKED_PLAYER_FROM_BED("&bYou kicked &a%player% &bout of their bed"),
    //YOU_ARE_KICKED_FROM_BED("&cYou were kicked out from your bed"),
    //KICKING_NOT_ALLOWED("&cYou do not have permission to kick this player out of their bed"),
    //FLAG_DOES_NOT_EXIST("&cThis flag does not exist"),
    //FLAG_SET_IN_WORLD("&bThe &e%flag% &bflag value in your world is &e%value%&b."),
    //FLAG_RESET_IN_WORLD("&bThe &e%flag% &bflag has been reset to &e%default-value%");
    ;

    private final String message;

    MessageTemplate(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public MessageKey getKey() {
        return null;
    }
}
