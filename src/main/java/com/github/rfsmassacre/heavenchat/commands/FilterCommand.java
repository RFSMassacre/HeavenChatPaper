package com.github.rfsmassacre.heavenchat.commands;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.commands.PaperCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FilterCommand extends PaperCommand
{
	public FilterCommand()
	{
		super(HeavenChat.getInstance(), "filter");
	}

	private class ToggleCommand extends PaperSubCommand
	{
		public ToggleCommand()
		{
			super("toggle");
		}

		@Override
		public void onRun(CommandSender sender, String[] args)
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
		public List<String> onTabComplete(CommandSender sender, String[] args)
		{
			return Collections.emptyList();
		}
	}

	private class AddCommand extends PaperSubCommand
	{
		public AddCommand()
		{
			super("block");
		}

		@Override
		public void onRun(CommandSender sender, String[] args)
		{
			if (sender instanceof Player player)
			{
				if (args.length < 2)
				{
					locale.sendLocale(player, "filter.invalid-args");
					return;
				}

				String word = LocaleData.stripColors(args[1]);
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
		public List<String> onTabComplete(CommandSender sender, String[] args)
		{
			return Collections.emptyList();
		}
	}

	private class RemoveCommand extends PaperSubCommand
	{
		public RemoveCommand()
		{
			super("unblock");
		}

		@Override
		public void onRun(CommandSender sender, String[] args)
		{
			if (sender instanceof Player player)
			{
				if (args.length < 2)
				{
					locale.sendLocale(player, "filter.invalid-args");
					return;
				}

				String word = LocaleData.stripColors(args[1]);
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
		public List<String> onTabComplete(CommandSender sender, String[] args)
		{
			return Collections.emptyList();
		}
	}

	private class ListCommand extends PaperSubCommand
	{
		public ListCommand()
		{
			super("list");
		}

		@Override
		public void onRun(CommandSender sender, String[] args)
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
	}
}
