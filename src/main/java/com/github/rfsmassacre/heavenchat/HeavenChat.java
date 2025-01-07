package com.github.rfsmassacre.heavenchat;

import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.library.configs.Configuration;
import com.github.rfsmassacre.heavenchat.library.configs.Locale;
import com.github.rfsmassacre.heavenchat.library.managers.TextManager;
import com.github.rfsmassacre.heavenchat.listeners.ChatListener;
import com.github.rfsmassacre.heavenchat.listeners.LoginListener;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenchat.utils.LogUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
public class HeavenChat extend
{

    @Inject
    @Getter
    private final Logger logger;
    @Inject
    @DataDirectory
    @Getter
    private final Path dataDirectory;

    @Getter
    private static HeavenChat instance;

    @Getter
    private Configuration configuration;
    @Getter
    private Locale locale;
    @Getter
    private TextManager textManager;

    @Getter
    private PlaceholderAPI papi;

    @Inject
    public HeavenChat(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory)
    {
        instance = this;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event)
    {
        if (Files.notExists(dataDirectory))
        {
            try
            {
                Files.createDirectory(dataDirectory);
            }
            catch (IOException exception)
            {
                logger.debug("Data directory could not be created.");
            }
        }

        this.configuration = new Configuration("", "config.yml");
        this.locale = new Locale("", "locale.yml");
        this.textManager = new TextManager("logs");
        this.papi = PlaceholderAPI.createInstance();

        getServer().getEventManager().register(this, new ChatListener());
        getServer().getEventManager().register(this, new LoginListener());

        Channel.loadAllChannels();

        CommandManager commandManager = getServer().getCommandManager();
        commandManager.register("heavenchat", new MainCommand(), "heavenchat", "hc");
        commandManager.register("channel", new ChannelCommand(), "channel", "ch");
        commandManager.register("pm", new PMCommand(),"pm", "msg");
        commandManager.register("reply", new ReplyCommand(),"r");
        commandManager.register("ignore", new IgnoreCommand(), "ignore", "block");
        commandManager.register("unignore", new UnignoreCommand(), "unignore", "unblock");
        commandManager.register("filter", new FilterCommand());
        commandManager.register("spy", new SpyCommand());
        commandManager.register("broadcast", new BroadcastCommand());

        commandManager.register("hub", new AliasCommand("hub"));
        commandManager.register("factions", new AliasCommand("factions"));
        commandManager.register("creative", new AliasCommand("creative"));
        commandManager.register("games", new AliasCommand("games"));
        commandManager.register("survival", new AliasCommand("survival"));


        getServer().getScheduler().buildTask(this, LogUtil::writeAsync)
                .repeat(configuration.getLong("log-interval"), TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event)
    {
        Channel.saveAllChannels();
        for (ChannelMember member : ChannelMember.getMembers())
        {
            ChannelMember.getData().write(member.getPlayerId().toString(), member);
        }

        LogUtil.write();
    }
}
