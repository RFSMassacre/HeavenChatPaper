package com.github.rfsmassacre.heavenchat2.commands;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelCommand extends HeavenCommand
{
	public ChannelCommand() 
	{
		super("channel");

		addSubCommand(new CurrentListCommand());
		addSubCommand(new AvailableListCommand());
		addSubCommand(new JoinCommand());
		addSubCommand(new LeaveCommand());
		addSubCommand(new AddCommand());
		addSubCommand(new KickCommand());
		addSubCommand(new FocusCommand());
	}
	
	/*
	 * Current Channels List
	 */
	private class CurrentListCommand extends SubCommand
	{
		public CurrentListCommand()
		{
			super("currentlist");
		}

		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			if (sender instanceof Player player)
			{
				ArrayList<String> currentChannels = new ArrayList<String>();
				for (Channel channel : Channel.getChannels())
				{
					if (channel.isMemberId(player.getUniqueId()))
					{
						currentChannels.add(channel.getDisplayName());
					}
				}
				
				locale.sendLocale(sender, "channel.current-list", "{channels}",
						String.join("&f, ", currentChannels));
				return;
			}
			
			//Send console error
			locale.sendLocale(sender, "error.console");
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			List<String> suggestions = new ArrayList<>();
			if (sender instanceof Player player)
			{
				if (args.length == 2)
				{
					for (Channel channel : Channel.getChannels())
					{
						if (channel.isMemberId(player.getUniqueId()))
						{
							suggestions.add(channel.getName());
						}
					}
				}
			}

			return suggestions;
		}
	}
	
	/*
	 * Available Channels List
	 */
	private class AvailableListCommand extends SubCommand
	{
		public AvailableListCommand()
		{
			super("list");
		}

		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			if (sender instanceof Player player)
			{
				ArrayList<String> availableChannels = new ArrayList<String>();
				ChannelMember member = ChannelMember.getMember(player.getUniqueId());
				for (Channel channel : Channel.getChannels())
				{
					if (channel.canJoin(member))
						availableChannels.add(channel.getDisplayName());
				}
				
				locale.sendLocale(member.getPlayer(), "channel.available-list", "{channels}",
						String.join("&f, ", availableChannels));
				return;
			}
			
			//Send console error
			locale.sendLocale(sender, "invalid.console");
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			List<String> suggestions = new ArrayList<>();
			if (sender instanceof Player player)
			{
				if (args.length == 2)
				{
					ChannelMember member = ChannelMember.getMember(player.getUniqueId());
					if (member == null)
					{
						return Collections.emptyList();
					}

					for (Channel channel : Channel.getChannels())
					{
						if (channel.canJoin(member))
						{
							suggestions.add(channel.getName());
						}
					}
				}
			}

			return suggestions;
		}
	}
	
	/*
	 * Channel Join
	 */
	private class JoinCommand extends SubCommand
	{
		public JoinCommand()
		{
			super("join");
		}

		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			if (sender instanceof Player player)
			{
				if (args.length >= 2)
				{
					Channel channel = Channel.getChannel(args[1]);
					ChannelMember member = ChannelMember.getMember(player.getUniqueId());
					if (channel != null)
					{
						if (channel.canJoin(member))
						{
							//Add to channel
							channel.addMemberIds(player.getUniqueId());
							member.setFocusedChannel(channel);
							member.setFocusedMember(null);
							locale.sendLocale(sender, "channel.joined", "{channel}",
									channel.getDisplayName());
							return;
						}
						
						//No perm error
						locale.sendLocale(sender, "channel.no-perm", "{channel}", channel.getDisplayName());
						return;
					}
					
					//Send channel not found error
					locale.sendLocale(sender, "channel.not-found", "{arg}", args[1]);
					return;
				}
				
				//Invalid arg error
				locale.sendLocale(sender, "error.invalid-args");
				return;
			}
			
			//Send console error
			locale.sendLocale(sender, "error.console");
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			List<String> suggestions = new ArrayList<>();
			if (sender instanceof Player player)
			{
				if (args.length == 2)
				{
					ChannelMember member = ChannelMember.getMember(player.getUniqueId());
					if (member == null)
					{
						return Collections.emptyList();
					}

					for (Channel channel : Channel.getChannels())
					{
						if (channel.canJoin(member))
						{
							suggestions.add(channel.getName());
						}
					}
				}
			}

			return suggestions;
		}
	}
	
	/*
	 * Channel Leave
	 */
	private class LeaveCommand extends SubCommand
	{
		public LeaveCommand()
		{
			super("leave");
		}

		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			if (sender instanceof Player player)
			{
				if (args.length >= 2)
				{
					Channel channel = Channel.getChannel(args[1]);
					ChannelMember member = ChannelMember.getMember(player.getUniqueId());
					
					if (channel != null)
					{
						if (channel.canLeave(member))
						{
							//Add to channel
							channel.removeMemberId(player.getUniqueId());
							locale.sendLocale(sender, "channel.left", "{channel}",
									channel.getDisplayName());
							
							//Clear focused channel only if it's the one member is leaving
							if (channel.equals(member.getFocusedChannel()))
							{
								member.setFocusedChannel(null);
							}
							
							return;
						}
						
						//No perm error
						locale.sendLocale(sender, "channel.no-perm", "{channel}", channel.getDisplayName());
						return;
					}
					
					//Send channel not found error
					locale.sendLocale(sender, "channel.not-found", "{arg}", args[1]);
					return;
				}
				
				//Invalid arg error
				locale.sendLocale(sender, "error.invalid-args");
				return;
			}
			
			//Send console error
			locale.sendLocale(sender, "error.console");
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			List<String> suggestions = new ArrayList<>();
			if (sender instanceof Player player)
			{
				if (args.length == 2)
				{
					ChannelMember member = ChannelMember.getMember(player.getUniqueId());
					if (member == null)
					{
						return suggestions;
					}

					for (Channel channel : Channel.getChannels())
					{
						if (channel.canLeave(member))
						{
							suggestions.add(channel.getName());
						}
					}
				}
			}

			return suggestions;
		}
	}
	
	/*
	 * Channel Kick
	 */
	private class KickCommand extends SubCommand
	{
		public KickCommand()
		{
			super("kick" );
		}

		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			if (args.length >= 2)
			{
				Channel channel = Channel.getChannel(args[2]);
				String memberName = args[1];
				if (channel == null)
				{
					//Send channel not found error
					locale.sendLocale(sender, "channel.not-found", "{arg}", args[1]);
					return;
				}

				ChannelMember member = channel.findMember(memberName);
				if (member == null)
				{
					//Member not found error
					locale.sendLocale(sender, "channel.member-not-found", "{arg}", memberName);
					return;
				}

				if (!channel.isMemberId(member.getPlayerId()))
				{
					//Give not in channel error
					locale.sendLocale(sender, "channel.not-in-channel", "{member}", member.getName(),
							"{channel}", channel.getDisplayName());
					return;
				}

				if (channel.canLeave(member))
				{
					//Remove from channel
					channel.removeMemberId(member.getPlayerId());
					locale.sendLocale(member.getPlayer(), "channel.kicked", "{channel}",
							channel.getDisplayName());

					//Clear focused channel only if it's the one member is leaving
					if (channel.equals(member.getFocusedChannel()))
					{
						member.setFocusedChannel(null);
					}

					return;
				}

				//No perm error
				locale.sendLocale(sender, "channel.no-perm", "{channel}", channel.getDisplayName());
			}
			
			//Invalid arg error
			locale.sendLocale(sender, "error.invalid-args");
			return;
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			List<String> suggestions = new ArrayList<>();
			if (sender instanceof Player player)
			{
				if (args.length == 2)
				{
					suggestions.addAll(HeavenChat.getInstance().getServer().getAllPlayers().stream()
							.map(Player::getUsername).toList());
				}

				if (args.length == 3)
				{
					for (Channel channel : Channel.getChannels())
					{
						ChannelMember member = channel.findMember(args[1]);
						if (member == null)
						{
							continue;
						}

						if (channel.isMemberId(member.getPlayerId()) && channel.canLeave(member))
						{
							suggestions.add(channel.getName());
						}
					}
				}
			}

			return suggestions;
		}
	}
	
	/*
	 * Channel Add
	 */
	private class AddCommand extends SubCommand
	{
		public AddCommand()
		{
			super("add");
		}

		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			if (args.length >= 3)
			{
				Channel channel = Channel.getChannel(args[2]);
				String memberName = args[1];
				ChannelMember member = ChannelMember.findMember(memberName);
				if (channel == null)
				{
					//Send channel not found error
					locale.sendLocale(sender, "channel.not-found", "{arg}", args[1]);
					return;
				}

				if (member == null)
				{
					//Member not found error
					locale.sendLocale(sender, "channel.member-not-found", "{arg}", memberName);
					return;
				}

				if (channel.isMemberId(member.getPlayerId()))
				{
					//Give already in channel error
					locale.sendLocale(sender, "channel.already-in-channel", "{member}", member.getName(),
							"{channel}", channel.getDisplayName());
					return;
				}

				if (channel.canJoin(member))
				{
					//Add to channel
					channel.addMemberIds(member.getPlayerId());
					locale.sendLocale(member.getPlayer(), "channel.added", "{channel}",
							channel.getDisplayName());
					member.setFocusedChannel(channel);
					member.setFocusedMember(null);
					return;
				}


				//No perm error
				locale.sendLocale(sender, "target-channel.no-perm", "{target}", member.getName(),
						"{channel}", channel.getDisplayName());
			}

			//Invalid arg error
			locale.sendLocale(sender, "error.invalid-args");
			return;
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			List<String> suggestions = new ArrayList<>();
			if (sender instanceof Player player)
			{
				if (args.length == 2)
				{
					for (Player online : HeavenChat.getInstance().getServer().getAllPlayers())
					{
						suggestions.add(online.getUsername());
					}
				}

				if (args.length == 3)
				{
					ChannelMember member = ChannelMember.findMember(args[2]);
					if (member == null)
					{
						return Collections.emptyList();
					}

					for (Channel channel : Channel.getChannels())
					{
						if (!channel.isMemberId(member.getPlayerId()) && channel.canJoin(member))
						{
							suggestions.add(channel.getName());
						}
					}
				}
			}

			return suggestions;
		}
	}
	
	/*
	 * Channel Focus Command
	 */
	private class FocusCommand extends SubCommand
	{
		public FocusCommand()
		{
			super("focus");
		}

		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			if (args.length >= 3)
			{
				Channel channel = Channel.getChannel(args[2]);
				String memberName = args[1];
				ChannelMember member = ChannelMember.findMember(memberName);
				if (channel == null)
				{
					//Send channel not found error
					locale.sendLocale(sender, "channel.not-found", "{arg}", args[1]);
					return;
				}
				if (member == null)
				{
					//Member not found error
					locale.sendLocale(sender, "channel.member-not-found", "{arg}", memberName);
					return;
				}

				if (channel.isMemberId(member.getPlayerId()))
				{
					//Set their focused channel to this one
					if (!member.getFocusedChannel().equals(channel))
					{
						member.setFocusedChannel(channel);

						locale.sendLocale(member.getPlayer(), member.getPlayer(), true, "channel.focused", "{channel}",
								channel.getDisplayName());
						locale.sendLocale(sender, member.getPlayer(), true, "channel.focuse-target",
								"{target}", member.getName(), "{channel}", channel.getDisplayName());
						return;
					}

					//Already focused error
					locale.sendLocale(sender, member.getPlayer(), true, "channel.already-focused-target", "{target}",
							member.getName(), "{channel}", channel.getDisplayName());
					return;
				}

				//No perm error
				locale.sendLocale(sender, member.getPlayer(), true,  "channel.target-no-perm",
						"{target}", member.getName(), "{channel}", channel.getDisplayName());
			}

			//Invalid arg error
			locale.sendLocale(sender, "error.invalid-args");
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			List<String> suggestions = new ArrayList<>();
			if (sender instanceof Player player)
			{
				if (args.length == 2)
				{
					ChannelMember member = ChannelMember.getMember(player.getUniqueId());
					if (member == null)
					{
						return suggestions;
					}

					for (Channel channel : Channel.getChannels())
					{
						if (channel.isMemberId(member.getPlayerId()) && !member.getFocusedChannel().equals(channel))
						{
							suggestions.add(channel.getName());
						}
					}
				}
			}

			return suggestions;
		}
	}
}
