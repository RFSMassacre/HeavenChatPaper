package com.github.rfsmassacre.heavenchat.commands;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenlibrary.paper.commands.SimplePaperCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PMCommand extends SimplePaperCommand
{
	public PMCommand() 
	{
		super(HeavenChat.getInstance(), "pm");
	}

	@Override
	public void onRun(CommandSender sender, String... args)
	{
		if (sender instanceof Player player)
		{
			ChannelMember member = ChannelMember.getMember(player.getUniqueId());
			if (args.length == 0)
			{
				if (member.getFocusedMember() == null)
				{
					//Send not chatting error
					locale.sendLocale(sender, "pm.not-chatting");
				}
				else
				{
					//Stop chatting with current member
					locale.sendLocale(sender, true, "pm.left", "{target}",
							member.getFocusedMember().getDisplayName());
					member.setFocusedMember(null);
				}

				return;
			}
			else if (args.length == 1)
			{
				Player targetPlayer = Bukkit.getPlayer(args[0]);
				if (targetPlayer != null)
				{
					ChannelMember target = ChannelMember.getMember(targetPlayer.getUniqueId());
					if (target.equals(member))
					{
						locale.sendLocale(sender, true, "pm.self");
						return;
					}

					if (!target.hasIgnored(member))
					{
						member.setFocusedMember(target);
						locale.sendLocale(sender, true, "pm.chatting", "{target}",
								target.getDisplayName());
					}

					else
					{
						//Send ignored error
						locale.sendLocale(sender, true, "pm.ignored", "{target}",
								target.getDisplayName());
					}
				}
				else
				{
					//Send not found error
					locale.sendLocale(sender, "error.not-found", "{arg}", args[0]);
				}

				return;
			}
			else
			{
				Player targetPlayer = Bukkit.getPlayer(args[0]);
				if (targetPlayer != null)
				{
					ChannelMember target = ChannelMember.getMember(targetPlayer.getUniqueId());
					if (target.equals(member))
					{
						locale.sendLocale(sender, "pm.self");
						return;
					}

					if (!target.hasIgnored(member))
					{
						//Convert args after the first arg as message
						StringBuilder privateMessage = new StringBuilder();
						for (int arg = 1; arg < args.length; arg++)
						{
							if (arg != (args.length - 1))
							{
								privateMessage.append(args[arg]).append(" ");
							}
							else
							{
								privateMessage.append(args[arg]);
							}
						}

						target.sendPrivateMessage(member, privateMessage.toString());
					}
					else
					{
						//Send ignored error
						locale.sendLocale(sender, true, "pm.ignored", "{target}",
								target.getDisplayName());
					}
				}
				else
				{
					//Send not found error
					locale.sendLocale(sender, "error.not-found", "{arg}", args[0]);
				}

				return;
			}
		}

		//Send console error
		locale.sendLocale(sender, "error.console");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String... args)
	{
		List<String> suggestions = new ArrayList<>();
		if (args.length == 1)
		{
			for (Player player : HeavenChat.getInstance().getServer().getOnlinePlayers())
			{
				if (!sender.equals(player))
				{
					suggestions.add(player.getName());
				}
			}
		}

		return suggestions;
	}
}
