package me.mrgeneralq.sleepmost.core.statics;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatColorUtils
{
    //Container of static methods
    private ChatColorUtils(){}

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String colorize(String text)
    {
        if (text == null || text.isEmpty()) {
            return "";
        }

        text = colorizeChatColors(text);

        if(ServerVersion.CURRENT_VERSION.supportsHexColors())
            text = colorizeHexColors(text);

        return text;
    }
    public static String colorizeChatColors(String text)
    {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    public static String colorizeHexColors(String text)
    {
        if (text == null || text.isEmpty()) {
            return "";
        }

        Matcher matcher = HEX_COLOR_PATTERN.matcher(text);
        if (!matcher.find()) {
            return text;
        }

        StringBuffer result = new StringBuffer(text.length());
        do {
            matcher.appendReplacement(result,
                    Matcher.quoteReplacement(net.md_5.bungee.api.ChatColor.of(matcher.group()).toString()));
        } while (matcher.find());
        matcher.appendTail(result);
        return result.toString();
    }
}
