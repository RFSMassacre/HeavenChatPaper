package com.github.rfsmassacre.heavenchat2.utils;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.library.configs.Locale;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        HeavenChat.getInstance().getTextManager().write(dateFormat.format(date) + ".txt", LOG);
        LOG.clear();
    }

    public static void writeAsync()
    {
        HeavenChat.getInstance().getServer().getScheduler().buildTask(HeavenChat.getInstance(), LogUtil::write)
                .delay(1L, TimeUnit.SECONDS).schedule();
    }
}
