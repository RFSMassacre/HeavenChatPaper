package com.github.rfsmassacre.heavenchat.commands;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenlibrary.paper.commands.SimplePaperCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnignoreCommand extends SimplePaperCommand
{
	public UnignoreCommand() 
	{
		super(HeavenChat.getInstance(), "unignore");
	}

	@Override
	public void onRun(CommandSender sender, String... args)
	{
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
						locale.sendLocale(sender, true,"ignore.removed", "{target}",
								target.getDisplayName());
					}
					else
					{
						locale.sendLocale(sender, true,"ignore.not-ignored", "{target}",
								target.getDisplayName());
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
	public List<String> onTabComplete(CommandSender sender, String... args)
	{
		List<String> suggestions = new ArrayList<>();
		if (!(sender instanceof Player player))
		{
			return Collections.emptyList();
		}

		if (args.length == 2)
		{
			ChannelMember member = ChannelMember.getMember(player.getUniqueId());
			for (Player online : HeavenChat.getInstance().getServer().getOnlinePlayers())
			{
				ChannelMember target = ChannelMember.getMember(online.getUniqueId());
				if (target == null)
				{
					continue;
				}

				if (member.hasIgnored(target) && !player.equals(online))
				{
					suggestions.add(player.getName());
				}
			}
		}

		return suggestions;
	}
}
