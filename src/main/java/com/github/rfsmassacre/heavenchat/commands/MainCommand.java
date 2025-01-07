package com.github.rfsmassacre.heavenchat.commands;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenlibrary.paper.commands.PaperCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class MainCommand extends PaperCommand
{
	public MainCommand() 
	{
		super(HeavenChat.getInstance(), "heavenchat");
	}
	
	/*
	 * Reload Command
	 */
	private class ReloadCommand extends PaperSubCommand
	{
		public ReloadCommand()
		{
			super("reload", "heavenchat.admin");
		}
		
		@Override
		protected void onRun(CommandSender sender, String[] args)
		{
			//Reloads config and locale
			HeavenChat.getInstance().getConfiguration().reload();
			HeavenChat.getInstance().getLocale().reload();
			
			//Reloads channel settings (list of members may roll back.)
			Channel.loadAllChannels();
			
			locale.sendLocale(sender, "admin.reload");
		}
	}
}
