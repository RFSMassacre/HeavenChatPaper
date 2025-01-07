package com.github.rfsmassacre.heavenchat.commands;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenlibrary.paper.commands.SimplePaperCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class IgnoreCommand extends SimplePaperCommand
{
	public IgnoreCommand() 
	{
		super(HeavenChat.getInstance(), "ignore");
	}

	@Override
	public void onRun(CommandSender sender, String... args)
	{
		if (sender instanceof Player player)
		{
			if (args.length >= 1)
			{
				if (args[0].equals("list"))
				{
					Bukkit.getScheduler().runTaskAsynchronously(HeavenChat.getInstance(), () ->
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
					});

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
						locale.sendLocale(sender, true, "ignore.cant-ignore", "{target}",
								target.getDisplayName());
					}
					else if (!member.hasIgnored(target))
					{
						member.addIgnoredMember(target);
						locale.sendLocale(sender, true,"ignore.added", "{target}",
								target.getDisplayName());
					}
					else
					{
						locale.sendLocale(sender, true,"ignore.already-added", "{target}",
								target.getDisplayName());
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
	public List<String> onTabComplete(CommandSender sender, String... args)
	{
		List<String> suggestions = new ArrayList<>();
		if (!(sender instanceof Player player))
		{
			return Collections.emptyList();
		}

		ChannelMember member = ChannelMember.getMember(player.getUniqueId());
		if (args.length == 1)
		{
			suggestions.add("list");
			for (Player online : HeavenChat.getInstance().getServer().getOnlinePlayers())
			{
				ChannelMember target = ChannelMember.getMember(online.getUniqueId());
				if (!member.hasIgnored(target) && !player.equals(online))
				{
					suggestions.add(online.getName());
				}
			}
		}

		return suggestions;
	}
}
