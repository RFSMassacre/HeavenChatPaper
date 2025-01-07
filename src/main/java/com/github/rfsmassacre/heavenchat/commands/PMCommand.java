package com.github.rfsmassacre.heavenchat2.commands;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.library.commands.VelocityCommand;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PMCommand extends VelocityCommand
{
	public PMCommand() 
	{
		super(HeavenChat.getInstance().getLocale(), "pm");
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
					locale.sendLocale(sender, member.getPlayer(), true, "pm.left", "{target}",
							member.getFocusedMember().getName());
					member.setFocusedMember(null);
				}

				return;
			}
			else if (args.length == 1)
			{
				Player targetPlayer = null;
				Optional<Player> optional = HeavenChat.getInstance().getServer().getPlayer(args[0]);
				if (optional.isPresent())
				{
					targetPlayer = optional.get();
				}

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
						locale.sendLocale(sender, target.getPlayer(), true, "pm.chatting",
								"{target}", target.getName());
					}
					else
					{
						//Send ignored error
						locale.sendLocale(sender, target.getPlayer(), true, "pm.ignored",
								"{target}", target.getName());
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
				Player targetPlayer = null;
				Optional<Player> optional = HeavenChat.getInstance().getServer().getPlayer(args[0]);
				if (optional.isPresent())
				{
					targetPlayer = optional.get();
				}

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
						locale.sendLocale(sender, target.getPlayer(), true, "pm.ignored",
								"{target}", target.getName());
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
	public List<String> suggest(Invocation invocation)
	{
		List<String> suggestions = new ArrayList<>();
		if (invocation.arguments().length == 1)
		{
			for (Player player : HeavenChat.getInstance().getServer().getAllPlayers())
			{
				if (!invocation.source().equals(player))
				{
					suggestions.add(player.getUsername());
				}
			}
		}

		return suggestions;
	}
}
