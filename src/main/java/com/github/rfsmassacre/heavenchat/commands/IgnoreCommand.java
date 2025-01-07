package com.github.rfsmassacre.heavenchat2.commands;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.library.commands.VelocityCommand;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class IgnoreCommand extends VelocityCommand
{
	public IgnoreCommand() 
	{
		super(HeavenChat.getInstance().getLocale(), "ignore");
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
				if (args[0].equals("list"))
				{
					HeavenChat.getInstance().getServer().getScheduler().buildTask(HeavenChat.getInstance(), () ->
					{
						ChannelMember member = ChannelMember.getMember(player.getUniqueId());
						List<UUID> ignoredIds = member.getIgnoredPlayerIds();
						List<String> names = new ArrayList<>();

						for (UUID playerId : ignoredIds)
						{
							ChannelMember ignoredMember = ChannelMember.getMember(playerId);
							if (ignoredMember == null)
							{
								ignoredMember = ChannelMember.getData().read(playerId.toString());
							}

							names.add(ignoredMember.getName());
						}

						if (names.isEmpty())
						{
							locale.sendLocale(sender, "ignore.empty");
						}
						else
						{
							locale.sendLocale(sender, "ignore.list", "{list}",
									String.join("&7, &r", names));
						}
					}).delay(0L, TimeUnit.SECONDS).schedule();

					return;
				}

				ChannelMember member = ChannelMember.getMember(player.getUniqueId());
				ChannelMember target = ChannelMember.findMember(args[0]);
				if (target != null)
				{
					if (member.equals(target))
					{
						locale.sendLocale(sender, "ignore.cant-self");
					}
					else if (target.getPlayer().hasPermission("heavenchat.ignore.immune"))
					{
						locale.sendLocale(sender, target.getPlayer(), true, "ignore.cant-ignore",
								"{target}", target.getName());
					}
					else if (!member.hasIgnored(target))
					{
						member.addIgnoredMember(target);
						locale.sendLocale(sender, target.getPlayer(), true,"ignore.added",
								"{target}", target.getName());
					}
					else
					{
						locale.sendLocale(sender, target.getPlayer(), true,"ignore.already-added",
								"{target}", target.getName());
					}
				}
				else
				{
					locale.sendLocale(sender, "error.not-online", "{arg}", args[0]);
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
		List<String> suggestions = new ArrayList<>();
		CommandSource sender = invocation.source();
		String[] args = invocation.arguments();
		if (!(sender instanceof Player player))
		{
			return Collections.emptyList();
		}

		ChannelMember member = ChannelMember.getMember(player.getUniqueId());
		if (args.length == 1)
		{
			suggestions.add("list");
			for (Player online : HeavenChat.getInstance().getServer().getAllPlayers())
			{
				ChannelMember target = ChannelMember.getMember(online.getUniqueId());
				if (!member.hasIgnored(target) && !player.equals(online))
				{
					suggestions.add(online.getUsername());
				}
			}
		}

		return suggestions;
	}
}
