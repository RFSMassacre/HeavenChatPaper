package com.github.rfsmassacre.heavenchat;

import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.commands.*;
import com.github.rfsmassacre.heavenchat.data.LogManager;
import com.github.rfsmassacre.heavenchat.listeners.ChatListener;
import com.github.rfsmassacre.heavenchat.listeners.LoginListener;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenchat.utils.LogUtil;
import com.github.rfsmassacre.heavenlibrary.paper.HeavenPaperPlugin;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.Files;

@Getter
public class HeavenChat extends HeavenPaperPlugin
{
    @Getter
    private static HeavenChat instance;
    private LogManager logManager;

    @Override
    public void onEnable()
    {
        instance = this;
        if (Files.notExists(getDataFolder().toPath()))
        {
            try
            {
                Files.createDirectory(getDataFolder().toPath());
            }
            catch (IOException exception)
            {
                //Do nothing
            }
        }

        addYamlManager(new PaperConfiguration(this, "", "config.yml", true));
        addYamlManager(new PaperLocale(this, "", "locale.yml", true));
        this.logManager = new LogManager(this, "logs");

        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new LoginListener(), this);

        Channel.loadAllChannels();

        getCommand("heavenchat").setExecutor(new MainCommand());
        getCommand("channel").setExecutor(new ChannelCommand());
        getCommand("pm").setExecutor(new PMCommand());
        getCommand("reply").setExecutor(new ReplyCommand());
        getCommand("ignore").setExecutor(new IgnoreCommand());
        getCommand("unignore").setExecutor(new UnignoreCommand());
        getCommand("filter").setExecutor(new FilterCommand());
        getCommand("spy").setExecutor(new SpyCommand());
        getCommand("broadcast").setExecutor(new BroadcastCommand());

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, LogUtil::writeAsync, 0L,
                getConfiguration().getInt("log-interval"));
    }

    @Override
    public void onDisable()
    {
        Channel.saveAllChannels(false);
        for (ChannelMember member : ChannelMember.getMembers())
        {
            ChannelMember.getData().write(member.getPlayerId().toString(), member);
        }

        LogUtil.write();
    }
}
