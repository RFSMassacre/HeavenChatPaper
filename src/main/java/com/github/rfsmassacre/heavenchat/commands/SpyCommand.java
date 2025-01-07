package com.github.rfsmassacre.heavenchat.commands;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenlibrary.paper.commands.SimplePaperCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpyCommand extends SimplePaperCommand
{
	public SpyCommand() 
	{
		super(HeavenChat.getInstance(), "spy");
	}

	@Override
	public void onRun(CommandSender sender, String... args)
	{
		if (!sender.hasPermission("heavenchat.spy"))
		{
			onFail(sender);
			return;
		}

		if (sender instanceof Player player)
		{
			ChannelMember member = ChannelMember.getMember(player.getUniqueId());
			if (!member.isSpying())
			{
				member.setSpying(true);
				locale.sendLocale(sender, "social-spy.enabled");
			}
			else
			{
				member.setSpying(false);
				locale.sendLocale(sender, "social-spy.disabled");
			}

			return;
		}

		//Send console error
		locale.sendLocale(sender, "error.console");
	}
}
