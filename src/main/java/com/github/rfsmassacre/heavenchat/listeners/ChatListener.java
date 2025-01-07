package com.github.rfsmassacre.heavenchat2.listeners;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.events.ChannelMessageEvent;
import com.github.rfsmassacre.heavenchat2.events.PrivateMessageEvent;
import com.github.rfsmassacre.heavenchat2.library.configs.Configuration;
import com.github.rfsmassacre.heavenchat2.library.configs.Locale;
import com.github.rfsmassacre.heavenchat2.library.managers.TextManager;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.github.rfsmassacre.heavenchat2.utils.LogUtil;
import com.github.rfsmassacre.heavenchat2.utils.SpamUtil;
import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent.GenericResult;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

import java.util.Optional;

public class ChatListener
{
	private final Configuration config;
	private final Locale locale;
	private final TextManager text;

	@Inject
	public ChatListener()
	{
		this.config = HeavenChat.getInstance().getConfiguration();
		this.locale = HeavenChat.getInstance().getLocale();
		this.text = HeavenChat.getInstance().getTextManager();
	}
	
	@Subscribe(order = PostOrder.LATE)
	public void onPlayerChat(PlayerChatEvent event)
	{
		if (!event.getResult().isAllowed())
		{
			return;
		}

		Player player = event.getPlayer();
		ChannelMember member = ChannelMember.getMember(player.getUniqueId());
		if (member == null)
		{
			return;
		}

		String chat = event.getMessage();
		if (member.getFocusedMemberId() != null)
		{
			ChannelMember targetMember = member.getFocusedMember();
			if (targetMember != null)
			{
				targetMember.sendPrivateMessage(member, chat);
			}
			else
			{
				ChannelMember.getData().readAsync(member.getFocusedMemberId().toString(), (offlineMember) ->
				{
					Channel channel = member.getFocusedChannel();

					//Get offline display name and channel name to properly tell user the target left
					String targetName = offlineMember.getName();
					String channelName = (channel != null ? channel.getDisplayName() : "");

					locale.sendLocale(member.getPlayer(), "pm.target-left", "{target}", targetName
							, "{channel}", channelName);

					member.setFocusedMember(null);
					member.setLastMember(null);
				});
			}

			return;
		}
		//Send channel message
		else if (member.getFocusedChannel() != null)
		{
			Channel channel = member.getFocusedChannel();
			if (channel.isMemberId(member.getPlayerId()))
			{
				channel.sendMessage(member, chat);
				return;
			}
		}

		locale.sendLocale(player, "error.no-channel");
	}
	
	@Subscribe
	public void onPlayerSpy(PrivateMessageEvent event)
	{
		if (event.getResult().isAllowed())
		{
			ChannelMember sender = event.getSender();
			ChannelMember target = event.getTarget();
			
			//Cancel if either of them have no-spy permissions for their target/sender
			if (!sender.getPlayer().hasPermission("heavenchat.nospy." + target.getName())
			 && !target.getPlayer().hasPermission("heavenchat.nospy." + sender.getName()))
			{
				for (ChannelMember member : ChannelMember.getMembers())
				{
					//Ensures if permission was removed they won't continue to spy
					if (member.isSpying() && !sender.equals(member) && !target.equals(member))
					{
						member.sendSpyMessage(sender, target, event.getMessage());
					}
				}
			}
		}
	}

	@Subscribe
	public void onColoredChat(ChannelMessageEvent event)
	{
		if (event.getResult().equals(GenericResult.denied()))
		{
			return;
		}

		Player player = event.getMember().getPlayer();
		if (event.getResult().isAllowed() &&  player != null)
		{
			boolean color = player.hasPermission("heavenchat.chat.color");
			boolean bold = player.hasPermission("heavenchat.chat.bold");
			boolean italic = player.hasPermission("heavenchat.chat.italic");
			boolean underline = player.hasPermission("heavenchat.chat.underline");
			boolean strikethrough = player.hasPermission("heavenchat.chat.strikethrough");
			boolean magic = player.hasPermission("heavenchat.chat.magic");
			boolean hex = player.hasPermission("heavenchat.chat.hex");

			event.setMessage(Locale.format(event.getMessage(), color, bold, italic, underline, strikethrough,
					magic, hex));
		}
	}
	
	@Subscribe
	public void onColoredPM(PrivateMessageEvent event)
	{
		if (event.getResult().equals(GenericResult.denied()))
		{
			return;
		}

		Player player = event.getSender().getPlayer();
		if (event.getResult().isAllowed() && player != null)
		{
			boolean color = player.hasPermission("heavenchat.chat.color");
			boolean bold = player.hasPermission("heavenchat.chat.bold");
			boolean italic = player.hasPermission("heavenchat.chat.italic");
			boolean underline = player.hasPermission("heavenchat.chat.underline");
			boolean strikethrough = player.hasPermission("heavenchat.chat.strikethrough");
			boolean magic = player.hasPermission("heavenchat.chat.magic");
			boolean hex = player.hasPermission("heavenchat.chat.hex");

			event.setMessage(Locale.format(event.getMessage(), color, bold, italic, underline, strikethrough,
					magic, hex));
		}
	}

	@Subscribe(order = PostOrder.EARLY)
	public void onChannelSpam(ChannelMessageEvent event)
	{
		if (event.getResult().isAllowed())
		{
			ChannelMember member = event.getMember();

			//Cancel if not fully logged in yet
			if (member != null)
			{
				//Cancel spam check if it's a whitelisted command.
				String message = event.getMessage();

				//Check for each kind of spam before sending it to the sub servers
				if (SpamUtil.isFloodSpam(member))
				{
					locale.sendLocale(member.getPlayer(), "filter.flood", "{time}",
							SpamUtil.getFloodSpamTime(member));
					event.setResult(GenericResult.denied());
					return;
				}

				if (SpamUtil.isRepetitionSpam(member, message))
				{
					locale.sendLocale(member.getPlayer(), "filter.repetition");
					event.setResult(GenericResult.denied());
					return;
				}

				//Prechecks permission and filters out
				event.setMessage(SpamUtil.filterSpam(member, message));
				//Record the time of message
				SpamUtil.setLastMessage(member.getPlayerId(), message);
			}
		}
	}

	@Subscribe(order = PostOrder.LATE)
	public void onPMSpam(PrivateMessageEvent event)
	{
		if (event.getResult().isAllowed())
		{
			ChannelMember member = event.getSender();

			//Cancel if not fully logged in yet
			if (member != null)
			{
				//Cancel spam check if it's a whitelisted command.
				String message = event.getMessage();

				//Check for each kind of spam before sending it to the sub servers
				if (SpamUtil.isFloodSpam(member))
				{
					locale.sendLocale(member.getPlayer(), "filter.flood", "{time}",
							SpamUtil.getFloodSpamTime(member));
					event.setResult(GenericResult.denied());
					return;
				}

				if (SpamUtil.isRepetitionSpam(member, message))
				{
					locale.sendLocale(member.getPlayer(), "filter.repetition");
					event.setResult(GenericResult.denied());
					return;
				}

				//Prechecks permission and filters out
				event.setMessage(SpamUtil.filterSpam(member, message));
				//Record the time of message
				SpamUtil.setLastMessage(member.getPlayerId(), message);
			}
		}
	}

	/*
	 * Logs every message players send in the log files.
	 */
	@Subscribe(order = PostOrder.LAST)
	public void onChannelLog(ChannelMessageEvent event)
	{
		if (event.getResult().isAllowed())
		{
			ChannelMember sender = event.getMember();
			Optional<ServerConnection> server = sender.getPlayer().getCurrentServer();
			if (server.isPresent())
			{
				String format = "[" + event.getChannel().getShortcut().toUpperCase() + "] " + sender.getName() + ": " +
						event.getMessage();
				LogUtil.log(format);
			}

		}
	}

	@Subscribe(order = PostOrder.LAST)
	public void onPMLog(PrivateMessageEvent event)
	{
		if (event.getResult().isAllowed())
		{
			ChannelMember sender = event.getSender();
			ChannelMember receiver = event.getTarget();

			if (!sender.getPlayer().hasPermission("heavenchat.nospy." + receiver.getName())
					&& !receiver.getPlayer().hasPermission("heavenchat.nospy." + sender.getName()))
			{
				String format = "[PM] " + sender.getName() + " » " + receiver.getName() + ": " + event.getMessage();
				LogUtil.log(format);
			}
		}
	}
}
