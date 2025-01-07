package com.github.rfsmassacre.heavenchat2.commands;

import com.github.rfsmassacre.heavenchat2.library.configs.Locale;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FilterCommand extends HeavenCommand
{
	public FilterCommand()
	{
		super("filter");

		addSubCommand(new ToggleCommand());
		addSubCommand(new AddCommand());
		addSubCommand(new RemoveCommand());
		addSubCommand(new ListCommand());
	}

	private class ToggleCommand extends SubCommand
	{
		public ToggleCommand()
		{
			super("toggle");
		}

		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			if (sender instanceof Player player)
			{
				ChannelMember member = ChannelMember.getMember(player.getUniqueId());
				if (member.isFiltered())
				{
					member.setFiltered(false);
					locale.sendLocale(sender, "filter.profanity.disabled");
				}
				else
				{
					member.setFiltered(true);
					locale.sendLocale(sender, "filter.profanity.enabled");
				}

				return;
			}

			//Console error
			locale.sendLocale(sender, "error.console");
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			return Collections.emptyList();
		}
	}

	private class AddCommand extends SubCommand
	{
		public AddCommand()
		{
			super("block");
		}

		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			if (sender instanceof Player player)
			{
				if (args.length < 2)
				{
					locale.sendLocale(player, "filter.invalid-args");
					return;
				}

				String word = Locale.stripColors(args[1]);
				ChannelMember member = ChannelMember.getMember(player.getUniqueId());
				if (member.isBlockedWord(word))
				{
					locale.sendLocale(player, "filter.personal.already-added", "{word}", word);
				}
				else
				{
					member.blockWord(word);
					locale.sendLocale(player, "filter.personal.added", "{word}", word);
				}

				return;
			}

			//Console error
			locale.sendLocale(sender, "error.console");
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			return Collections.emptyList();
		}
	}

	private class RemoveCommand extends SubCommand
	{
		public RemoveCommand()
		{
			super("unblock");
		}

		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			if (sender instanceof Player player)
			{
				if (args.length < 2)
				{
					locale.sendLocale(player, "filter.invalid-args");
					return;
				}

				String word = Locale.stripColors(args[1]);
				ChannelMember member = ChannelMember.getMember(player.getUniqueId());
				if (!member.isBlockedWord(word))
				{
					locale.sendLocale(player, "filter.personal.not-blocked", "{word}", word);
				}
				else
				{
					member.unblockWord(word);
					locale.sendLocale(player, "filter.personal.removed", "{word}", word);
				}

				return;
			}

			//Console error
			locale.sendLocale(sender, "error.console");
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			return Collections.emptyList();
		}
	}

	private class ListCommand extends SubCommand
	{
		public ListCommand()
		{
			super("list");
		}

		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			if (sender instanceof Player player)
			{
				ChannelMember member = ChannelMember.getMember(player.getUniqueId());
				Set<String> blockedWords = member.getBlockedWords();
				if (blockedWords.isEmpty())
				{
					locale.sendLocale(player, "filter.personal.empty");
					return;
				}

				locale.sendLocale(player, "filter.personal.list", "{list}", String.join("&7, &f",
						blockedWords));
				return;
			}

			//Console error
			locale.sendLocale(sender, "error.console");
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			return Collections.emptyList();
		}
	}
}
