package com.github.rfsmassacre.heavenchat.data;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenlibrary.paper.managers.PaperGsonManager;

public class ChannelGson extends PaperGsonManager<Channel>
{
    /**
     * Constructor.
     */
    public ChannelGson()
    {
        super(HeavenChat.getInstance(), "channels", Channel.class);
    }
}
