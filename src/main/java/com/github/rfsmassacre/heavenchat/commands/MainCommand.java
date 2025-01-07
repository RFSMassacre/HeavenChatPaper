package com.github.rfsmassacre.heavenchat2.commands;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.velocitypowered.api.command.CommandSource;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainCommand extends HeavenCommand
{
	public MainCommand() 
	{
		super("heavenchat");

		addSubCommand(new ReloadCommand());
	}
	
	/*
	 * Reload Command
	 */
	private class ReloadCommand extends SubCommand
	{
		public ReloadCommand()
		{
			super("reload", "heavenchat.admin");
		}
		
		@Override
		protected void onRun(CommandSource sender, String[] args)
		{
			//Reloads config and locale
			HeavenChat.getInstance().getConfiguration().reload();
			HeavenChat.getInstance().getLocale().reload();
			
			//Reloads channel settings (list of members may roll back.)
			Channel.loadAllChannels();
			
			locale.sendLocale(sender, "admin.reload");
		}

		@Override
		protected List<String> onTabComplete(CommandSource sender, String[] args)
		{
			return Collections.emptyList();
		}
	}
}
