package com.github.rfsmassacre.heavenchat.commands;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenlibrary.paper.commands.SimplePaperCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BroadcastCommand extends SimplePaperCommand
{
    public BroadcastCommand()
    {
        super(HeavenChat.getInstance(), "broadcast");
    }

    @Override
    public void onRun(CommandSender sender, String... args)
    {
        if (!sender.hasPermission("heavenchat.broadcast"))
        {
            onFail(sender);
            return;
        }

        if (args.length == 0)
        {
            //Invalid args
            locale.sendLocale(sender, "broadcast.no-args");
            playSound(sender, SoundKey.INCOMPLETE);
            return;
        }

        for (Player player : HeavenChat.getInstance().getServer().getOnlinePlayers())
        {
            locale.sendMessage(player, String.join(" ", args));
        }
    }
}
