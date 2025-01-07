package com.github.rfsmassacre.heavenchat2.commands;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.library.commands.VelocityCommand;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnignoreCommand extends VelocityCommand
{
	public UnignoreCommand() 
	{
		super(HeavenChat.getInstance().getLocale(), "unignore");
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
		String[] args = invocation.arguments();
		if (sender instanceof Player player)
		{
			if (args.length >= 1)
			{
				ChannelMember member = ChannelMember.getMember(player.getUniqueId());
				ChannelMember target = ChannelMember.findMember(args[0]);
				if (target != null)
				{
					if (member.equals(target))
					{
						locale.sendLocale(sender, "ignore.cant-self");
					}
					else if (member.hasIgnored(target))
					{
						member.removeIgnoredMember(target);
						locale.sendLocale(sender, target.getPlayer(), true,"ignore.removed",
								"{target}", target.getName());
					}
					else
					{
						locale.sendLocale(sender, target.getPlayer(), true,"ignore.not-ignored",
								"{target}", target.getName());
					}
				}
				else
				{
					locale.sendLocale(sender, "error.not-found", "{arg}", args[0]);
				}

				return;
			}

			//Invalid args
			locale.sendLocale(sender, "ignore.invalid-args");
			return;
		}

		//Send console error
		locale.sendLocale(sender, "error.console");
	}

	@Override
	public List<String> suggest(Invocation invocation)
	{
		CommandSource sender = invocation.source();
		String[] args = invocation.arguments();
		List<String> suggestions = new ArrayList<>();
		if (!(sender instanceof Player player))
		{
			return Collections.emptyList();
		}

		if (args.length == 2)
		{
			ChannelMember member = ChannelMember.getMember(player.getUniqueId());
			for (Player online : HeavenChat.getInstance().getServer().getAllPlayers())
			{
				ChannelMember target = ChannelMember.getMember(online.getUniqueId());
				if (target == null)
				{
					continue;
				}

				if (member.hasIgnored(target) && !player.equals(online))
				{
					suggestions.add(player.getUsername());
				}
			}
		}

		return suggestions;
	}
}
