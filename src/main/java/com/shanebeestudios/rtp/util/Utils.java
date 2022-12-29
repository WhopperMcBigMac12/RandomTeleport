package com.shanebeestudios.rtp.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Utils {

    private static final String PREFIX = "&7[&bRTP&7] ";

    private static String getColored(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void log(String format, Object... objects) {
        String message = String.format(format, objects);
        message = getColored(PREFIX + message);
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public static void sendMsg(CommandSender receiver, String format, Object... objects) {
        String message = String.format(format, objects);
        message = getColored(PREFIX + message);
        receiver.sendMessage(message);
    }

    public static void broadcast(String format, Object... objects) {
        log(format, objects);
        Bukkit.getOnlinePlayers().forEach(player -> sendMsg(player, format, objects));
    }

}
