package com.github.rfsmassacre.heavenchat2.commands;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.library.commands.VelocityCommand;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.Collections;
import java.util.List;

public class ReplyCommand extends VelocityCommand
{
	public ReplyCommand() 
	{
		super(HeavenChat.getInstance().getLocale(), "reply");
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

			//Send not chatting error
			if (member.getLastMemberId() == null)
			{
				locale.sendLocale(sender,"pm.not-chatting");
				return;
			}


			ChannelMember target = member.getLastMember();
			//Send not there error
			if (target == null)
			{
				ChannelMember.getData().readAsync(member.getLastMemberId().toString(), (lastMember) ->
				{
					if (lastMember != null)
					{
						Channel channel = member.getFocusedChannel();

						//Get offline display name and channel name to properly tell user the target left
						String targetName = lastMember.getName();
						String channelName = (channel != null ? channel.getDisplayName() : "");

						locale.sendLocale(member.getPlayer(), target.getPlayer(), true, "pm.target-left",
								"{target}", targetName, "{channel}", channelName);
					}

					member.setFocusedMember(null);
					member.setLastMember(null);
				});

				return;
			}

			//Send self error
			if (target.equals(member))
			{
				locale.sendLocale(sender, "pm.self");
				return;
			}

			//Send ignored error
			if (target.hasIgnored(member))
			{
				locale.sendLocale(sender, target.getPlayer(), true, "pm.ignored", "{target}",
						target.getName());
				return;
			}

			//Focus last member
			if (args.length == 0)
			{
				if (member.getFocusedMember() != null)
				{
					locale.sendLocale(sender, target.getPlayer(), true, "pm.left", "{target}",
							member.getFocusedMember().getName());
					member.setFocusedMember(null);
				}
				else
				{
					member.setFocusedMember(target);
					locale.sendLocale(sender, target.getPlayer(), true, "pm.chatting", "{target}",
							target.getName());
				}
			}
			//Send message to last member
			else
			{
				target.sendPrivateMessage(member, String.join(" ", args));
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
