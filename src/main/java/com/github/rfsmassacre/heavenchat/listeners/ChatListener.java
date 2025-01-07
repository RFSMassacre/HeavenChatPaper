package com.github.rfsmassacre.heavenchat.listeners;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.data.LogManager;
import com.github.rfsmassacre.heavenchat.events.ChannelMessageEvent;
import com.github.rfsmassacre.heavenchat.events.PrivateMessageEvent;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenchat.utils.LogUtil;
import com.github.rfsmassacre.heavenchat.utils.SpamUtil;
import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

public class ChatListener implements Listener
{
	private final PaperConfiguration config;
	private final PaperLocale locale;
	private final LogManager logs;

	public ChatListener()
	{
		this.config = HeavenChat.getInstance().getConfiguration();
		this.locale = HeavenChat.getInstance().getLocale();
		this.logs = HeavenChat.getInstance().getLogManager();
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
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

			event.setCancelled(true);
			return;
		}
		//Send channel message
		else if (member.getFocusedChannel() != null)
		{
			Channel channel = member.getFocusedChannel();
			if (channel.isMemberId(member.getPlayerId()))
			{
				channel.sendMessage(member, event.getMessage());
				event.setCancelled(true);
				return;
			}
		}

		locale.sendLocale(player, "error.no-channel");
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerSpy(PrivateMessageEvent event)
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

	@EventHandler(ignoreCancelled = true)
	public void onColoredChat(ChannelMessageEvent event)
	{
		Player player = event.getMember().getPlayer();
		if (player == null)
		{
			return;
		}

		boolean color = player.hasPermission("heavenchat.chat.color");
		boolean bold = player.hasPermission("heavenchat.chat.bold");
		boolean italic = player.hasPermission("heavenchat.chat.italic");
		boolean underline = player.hasPermission("heavenchat.chat.underline");
		boolean strikethrough = player.hasPermission("heavenchat.chat.strikethrough");
		boolean magic = player.hasPermission("heavenchat.chat.magic");
		boolean hex = player.hasPermission("heavenchat.chat.hex");
		event.setMessage(LocaleData.format(event.getMessage(), color, bold, italic, underline, strikethrough,
				magic, hex));
	}

	@EventHandler(ignoreCancelled = true)
	public void onColoredPM(PrivateMessageEvent event)
	{

		Player player = event.getSender().getPlayer();
		if (player == null)
		{
			return;
		}

		boolean color = player.hasPermission("heavenchat.chat.color");
		boolean bold = player.hasPermission("heavenchat.chat.bold");
		boolean italic = player.hasPermission("heavenchat.chat.italic");
		boolean underline = player.hasPermission("heavenchat.chat.underline");
		boolean strikethrough = player.hasPermission("heavenchat.chat.strikethrough");
		boolean magic = player.hasPermission("heavenchat.chat.magic");
		boolean hex = player.hasPermission("heavenchat.chat.hex");
		event.setMessage(LocaleData.format(event.getMessage(), color, bold, italic, underline, strikethrough,
				magic, hex));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onChannelSpam(ChannelMessageEvent event)
	{
		ChannelMember member = event.getMember();
		if (member == null)
		{
			return;
		}

		//Cancel spam check if it's a whitelisted command.
		String message = event.getMessage();

		//Check for each kind of spam before sending it to the sub servers
		if (SpamUtil.isFloodSpam(member))
		{
			locale.sendLocale(member.getPlayer(), "filter.flood", "{time}",
					SpamUtil.getFloodSpamTime(member));
			event.setCancelled(true);
			return;
		}

		if (SpamUtil.isRepetitionSpam(member, message))
		{
			locale.sendLocale(member.getPlayer(), "filter.repetition");
			event.setCancelled(true);
			return;
		}

		//Prechecks permission and filters out
		event.setMessage(SpamUtil.filterSpam(member, message));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPMSpam(PrivateMessageEvent event)
	{
		ChannelMember member = event.getSender();
		if (member == null)
		{
			return;
		}

		//Cancel spam check if it's a whitelisted command.
		String message = event.getMessage();

		//Check for each kind of spam before sending it to the sub servers
		if (SpamUtil.isFloodSpam(member))
		{
			locale.sendLocale(member.getPlayer(), "filter.flood", "{time}",
					SpamUtil.getFloodSpamTime(member));
			event.setCancelled(true);
			return;
		}

		String filtered = SpamUtil.filterSpam(member, message);
		if (SpamUtil.isRepetitionSpam(member, filtered))
		{
			locale.sendLocale(member.getPlayer(), "filter.repetition");
			event.setCancelled(true);
			return;
		}

		//Prechecks permission and filters out
		event.setMessage(SpamUtil.filterSpam(member, filtered));

		//Record the time of message
		SpamUtil.setLastMessage(member.getPlayerId(), filtered);
	}

	/*
	 * Logs every message players send in the log files.
	 */

	@EventHandler(ignoreCancelled = true)
	public void onChannelLog(ChannelMessageEvent event)
	{
		ChannelMember sender = event.getMember();
		String format = "[" + event.getChannel().getShortcut().toUpperCase() + "] " + sender.getName() + ": " +
				event.getMessage();
		LogUtil.log(format);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPMLog(PrivateMessageEvent event)
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

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPMPing(PrivateMessageEvent event)
	{
		ChannelMember sender = event.getSender();
		ChannelMember receiver = event.getTarget();

	}
}
