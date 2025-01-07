package com.github.rfsmassacre.heavenchat.data;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenlibrary.paper.managers.PaperGsonManager;

public class MemberGson extends PaperGsonManager<ChannelMember>
{
    /**
     * Constructor.
     */
    public MemberGson()
    {
        super(HeavenChat.getInstance(), "players", ChannelMember.class);
    }
}
