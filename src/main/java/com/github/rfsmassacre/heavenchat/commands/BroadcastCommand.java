package com.github.rfsmassacre.heavenchat2.commands;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.library.commands.VelocityCommand;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BroadcastCommand extends VelocityCommand
{
    public BroadcastCommand()
    {
        super(HeavenChat.getInstance().getLocale(), "broadcast");
    }

    @Override
    protected void onFail(CommandSource sender)
    {
        locale.sendLocale(sender, "commands.no-perm");
    }

    @Override
    protected void onInvalidArgs(CommandSource sender)
    {
        locale.sendLocale(sender, "commands.invalid-subcommand");
    }

    @Override
    public void execute(Invocation invocation)
    {
        CommandSource sender = invocation.source();
        if (!sender.hasPermission("heavenchat.broadcast"))
        {
            onFail(sender);
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 0)
        {
            //Invalid args
            locale.sendLocale(sender, "broadcast.no-args");
            return;
        }

        for (Player player : HeavenChat.getInstance().getServer().getAllPlayers())
        {
            locale.sendMessage(player, String.join(" ", args));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation)
    {
        return Collections.emptyList();
    }
}
