package com.github.rfsmassacre.heavenchat.utils;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import org.bukkit.Bukkit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogUtil
{
    private static final List<String> LOG = new ArrayList<>();

    public static void log(String message)
    {
        //Log message to file named today's date
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        HeavenChat.getInstance().getLogger().info(message);
        String log = "[" + timeFormat.format(date) + "] " + message;
        LOG.add(log);
    }

    public static void write()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        Date date = new Date();
        HeavenChat.getInstance().getLogManager().write(dateFormat.format(date) + ".txt", LOG);
        LOG.clear();
    }

    public static void writeAsync()
    {
        Bukkit.getScheduler().runTaskAsynchronously(HeavenChat.getInstance(), LogUtil::write);
    }
}
