package com.github.rfsmassacre.heavenchat.listeners;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginListener implements Listener
{
	private final PaperConfiguration config;
	private final PaperLocale locale;

	public LoginListener()
	{
		this.config = HeavenChat.getInstance().getConfiguration();
		this.locale = HeavenChat.getInstance().getLocale();
	}

	/*
	 * Rather than listening to proxy joining, this listens to
	 * the sub-server's notification. If the player's data hasn't
	 * been loaded in yet, it's assumed this is their first login.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerLoginEvent event)
	{
		Player player = event.getPlayer();
		ChannelMember.getData().readAsync(player.getUniqueId().toString(), (loginMember) ->
		{
			if (loginMember == null)
			{
				loginMember = new ChannelMember(player);
				loginMember.setDisplayName(player.getDisplayName());

				//Add member to all channels available
				for (Channel channel : Channel.getChannels())
				{
					if (channel.canJoin(loginMember))
					{
						channel.addMemberIds(loginMember.getPlayerId());
					}
				}

				Channel defaultChannel = Channel.getChannel(config.getString("default-channel"));
				defaultChannel.addMemberIds(loginMember.getPlayerId());
				loginMember.setFocusedChannel(defaultChannel);
				ChannelMember.getData().write(loginMember.getPlayerId().toString(), loginMember);
				if (config.getBoolean("enable-join-messages"))
				{
					for (Player online : HeavenChat.getInstance().getServer().getOnlinePlayers())
					{
						locale.sendLocale(online, false, "login.first-login-message",
								"{player}", loginMember.getDisplayName().replace("{nicknameprefix}",
										"&f◆"));
					}
				}
			}
			else
			{
				if (config.getBoolean("enable-join-messages"))
				{
					for (Player online : HeavenChat.getInstance().getServer().getOnlinePlayers())
					{
						locale.sendLocale(online, false,"login.login-message", "{player}",
								loginMember.getDisplayName().replace("{nicknameprefix}", "&f◆"));
					}
				}
			}

			loginMember.setDisplayName(player.getDisplayName());
			ChannelMember.addMember(loginMember);
		});

		//Player has not logged in yet

	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		ChannelMember member = ChannelMember.getMember(player.getUniqueId());

		//Unload data when player logs off the network
		if (member != null)
		{
			//Broadcast logout
			ChannelMember.getData().writeAsync(member.getPlayerId().toString(), member);
			ChannelMember.removeMember(member.getPlayerId());

			if (config.getBoolean("enable-join-messages"))
			{
				for (Player online : HeavenChat.getInstance().getServer().getOnlinePlayers())
				{
					locale.sendLocale(online, false, "login.logout-message", "{player}",
							member.getDisplayName());
				}
			}
		}
	}
}