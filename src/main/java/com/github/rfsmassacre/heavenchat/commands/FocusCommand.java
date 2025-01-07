package com.github.rfsmassacre.heavenchat.commands;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FocusCommand extends Command
{
	private final PaperLocale locale;
	private final Channel channel;
	private final String subPermission;
	
	public FocusCommand(Channel channel) 
	{
		super(channel.getShortcut());

		this.locale = HeavenChat.getInstance().getLocale();
		this.channel = channel;
		this.subPermission = "heavenchat.channel." + channel.getName();
	}

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args)
	{
		if (sender instanceof Player player)
		{
			ChannelMember member = ChannelMember.getMember(player.getUniqueId());
			if (!player.hasPermission(subPermission))
			{
				locale.sendLocale(player, "channel.no-perm", "{channel}", channel.getDisplayName());
				return true;
			}
			
			if (channel.isMemberId(member.getPlayerId()))
			{
				//Focus to this channel
				if (args.length == 0)
				{
					if (member.getFocusedMember() != null || member.getFocusedChannel() == null || 
					   !member.getFocusedChannel().equals(channel))
					{
						member.setFocusedChannel(channel);
						member.setFocusedMember(null);
						
						locale.sendLocale(player, "channel.focused", "{channel}", channel.getDisplayName());
					}
					else
					{
						locale.sendLocale(player, "channel.already-focused", "{channel}", channel.getDisplayName());
					}
				}
				//Send message to this channel
				else
				{
					String message = String.join(" ", args);
					channel.sendMessage(member, message);
				}
			}
			else
			{
				//Not in channel error
				locale.sendLocale(player, "channel.not-in-channel", "{channel}", channel.getDisplayName());
			}
		}
		else
		{
			//Console error
			locale.sendLocale(sender, "error.console");
		}

		return true;
	}
}
