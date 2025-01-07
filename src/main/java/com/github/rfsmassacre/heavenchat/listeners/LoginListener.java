package com.github.rfsmassacre.heavenchat2.listeners;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.library.configs.Configuration;
import com.github.rfsmassacre.heavenchat2.library.configs.Locale;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.google.inject.Inject;
import com.velocitypowered.api.event.ResultedEvent.ComponentResult;
import com.velocitypowered.api.event.ResultedEvent.GenericResult;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public class LoginListener
{
	private final Configuration config;
	private final Locale locale;

	@Inject
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
	@Subscribe
	public void onPlayerJoin(LoginEvent event)
	{
		if (!event.getResult().equals(ComponentResult.allowed()))
		{
			return;
		}

		Player player = event.getPlayer();
		ChannelMember.getData().readAsync(player.getUniqueId().toString(), (loginMember) ->
		{
			Logger logger = HeavenChat.getInstance().getLogger();
			if (loginMember == null)
			{
				loginMember = new ChannelMember(player);

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
					for (Player online : HeavenChat.getInstance().getServer().getAllPlayers())
					{
						locale.sendLocale(online, player,false,"login.first-login-message",
								"{player}", loginMember.getName());
					}
				}
			}
			else
			{
				logger.info(player.getUsername() + " has file!");
				if (config.getBoolean("enable-join-messages"))
				{
					for (Player online : HeavenChat.getInstance().getServer().getAllPlayers())
					{
						locale.sendLocale(online, player,false,"login.login-message", "{player}",
								player.getUsername());
					}
				}

				if (config.getBoolean("force-focus-channels"))
				{
					String serverName = "";
					Optional<ServerConnection> server = loginMember.getPlayer().getCurrentServer();
					if (server.isPresent())
					{
						serverName = server.get().getServerInfo().getName();
					}

					Channel serverChannel = Channel.getChannel(config.getString("servers." +
							serverName + ".channel"));
					List<String> ignoredChannels = config.getStringList("ignored-channels");
					if (serverChannel != null && serverChannel.isMemberId(loginMember.getPlayerId()) &&
							!ignoredChannels.contains(loginMember.getFocusedChannel().getName()))
					{
						if (serverChannel.canJoin(loginMember) && !serverChannel.isMemberId(loginMember.getPlayerId()))
						{
							//Add to channel
							serverChannel.addMemberIds(player.getUniqueId());
							loginMember.setFocusedChannel(serverChannel);
							loginMember.setFocusedMember(null);
							locale.sendLocale(loginMember.getPlayer(), player, true, "channel.joined",
									"{channel}", serverChannel.getDisplayName());
						}
					}
				}
			}

			ChannelMember.addMember(loginMember);
		});

		//Player has not logged in yet

	}

	@Subscribe
	public void onPlayerLeave(DisconnectEvent event)
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
				for (Player online : HeavenChat.getInstance().getServer().getAllPlayers())
				{
					locale.sendLocale(online, player,false, "login.logout-message", "{player}",
							member.getName());
				}
			}
		}
	}


	@Subscribe
	public void onPlayerSwitch(ServerConnectedEvent event)
	{
		Player player = event.getPlayer();
		ChannelMember member = ChannelMember.getMember(player.getUniqueId());

		//Update the server they are currently connected in
		if (member != null)
		{
			if (config.getBoolean("force-focus-channels"))
			{
				String serverName = "";
				Optional<ServerConnection> server = member.getPlayer().getCurrentServer();
				if (server.isPresent())
				{
					serverName = server.get().getServerInfo().getName();
				}

				Channel serverChannel = Channel.getChannel(config.getString("servers." +
						serverName + ".channel"));
				if (serverChannel != null && serverChannel.isMemberId(member.getPlayerId()) &&
						!config.getStringList("ignored-channels").contains(member.getFocusedChannel().getName()))
				{
					if (serverChannel.canJoin(member))
					{
						//Add to channel
						serverChannel.addMemberIds(player.getUniqueId());
						member.setFocusedChannel(serverChannel);
						member.setFocusedMember(null);
						locale.sendLocale(member.getPlayer(), false, "channel.joined", "{channel}",
								serverChannel.getDisplayName());
					}
				}
			}
		}
	}
}