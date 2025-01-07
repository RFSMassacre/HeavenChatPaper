package com.github.rfsmassacre.heavenchat2.commands;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.library.commands.VelocityCommand;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.Collections;
import java.util.List;

public class SpyCommand extends VelocityCommand
{
	public SpyCommand() 
	{
		super(HeavenChat.getInstance().getLocale(), "spy");
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
		if (!sender.hasPermission("heavenchat.spy"))
		{
			onFail(sender);
			return;
		}

		if (sender instanceof Player player)
		{
			ChannelMember member = ChannelMember.getMember(player.getUniqueId());
			if (!member.isSpying())
			{
				member.setSpying(true);
				locale.sendLocale(sender, "social-spy.enabled");
			}
			else
			{
				member.setSpying(false);
				locale.sendLocale(sender, "social-spy.disabled");
			}

			return;
		}

		//Send console error
		locale.sendLocale(sender, "error.console");
	}
	
	@Override
	public List<String> suggest(Invocation invocation)
	{
		return Collections.emptyList();
	}
}
