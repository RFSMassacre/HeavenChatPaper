package com.github.rfsmassacre.heavenchat2.data;

import com.github.rfsmassacre.heavenchat2.library.managers.GsonManager;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;

public class MemberGson extends GsonManager<ChannelMember>
{
    /**
     * Constructor.
     */
    public MemberGson()
    {
        super("players", ChannelMember.class);
    }
}
