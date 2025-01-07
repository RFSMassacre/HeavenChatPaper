package com.github.rfsmassacre.heavenchat2.commands;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.library.commands.VelocityCommand;
import com.github.rfsmassacre.heavenchat2.library.configs.Locale;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class FocusCommand extends VelocityCommand
{
	private final Channel channel;
	private final String subPermission;
	
	public FocusCommand(Channel channel) 
	{
		super(HeavenChat.getInstance().getLocale(), channel.getShortcut());
		
		this.channel = channel;
		this.subPermission = "heavenchat.channel." + channel.getName();
	}

	@Override
	public void execute(Invocation invocation)
	{
		if (invocation.source() instanceof Player player)
		{
			ChannelMember member = ChannelMember.getMember(player.getUniqueId());
			if (!player.hasPermission(subPermission))
			{
				locale.sendLocale(player, "channel.no-perm", "{channel}", channel.getDisplayName());
				return;
			}
			
			if (channel.isMemberId(member.getPlayerId()))
			{
				//Focus to this channel
				if (invocation.arguments().length == 0)
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
					String message = String.join(" ", invocation.arguments());
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
			locale.sendLocale(invocation.source(), "error.console");
		}
	}

	@Override
	protected void onFail(CommandSource sender)
	{
		locale.sendLocale(sender, "channel.no-perm", "{channel}", channel.getDisplayName());
	}

	@Override
	protected void onInvalidArgs(CommandSource sender)
	{
		locale.sendLocale(sender, "commands.invalid-subcommand");
	}
}
