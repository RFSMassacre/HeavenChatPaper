package com.github.rfsmassacre.heavenchat.commands;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenlibrary.paper.commands.SimplePaperCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyCommand extends SimplePaperCommand
{
	public ReplyCommand() 
	{
		super(HeavenChat.getInstance(), "reply");
	}

	@Override
	public void onRun(CommandSender sender, String... args)
	{
		if (sender instanceof Player player)
		{
			ChannelMember member = ChannelMember.getMember(player.getUniqueId());

			//Send not chatting error
			if (member.getLastMemberId() == null)
			{
				locale.sendLocale(sender,"pm.not-chatting");
				return ;
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
						String targetName = lastMember.getDisplayName();
						String channelName = (channel != null ? channel.getDisplayName() : "");

						locale.sendLocale(member.getPlayer(), true, "pm.target-left", "{target}",
								targetName, "{channel}", channelName);
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
				locale.sendLocale(sender, true, "pm.ignored", "{target}", target.getDisplayName());
				return;
			}

			//Focus last member
			if (args.length == 0)
			{
				if (member.getFocusedMember() != null)
				{
					locale.sendLocale(sender, true, "pm.left", "{target}",
							member.getFocusedMember().getDisplayName());
					member.setFocusedMember(null);
				}
				else
				{
					member.setFocusedMember(target);
					locale.sendLocale(sender, true, "pm.chatting", "{target}",
							target.getDisplayName());
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
}
