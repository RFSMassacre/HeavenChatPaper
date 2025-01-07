package com.github.rfsmassacre.heavenchat2.events;

import com.github.rfsmassacre.heavenchat2.players.ChannelMember;

public class PrivateMessageEvent extends MessageEvent
{
	public PrivateMessageEvent(ChannelMember sender, ChannelMember target, String message)
	{
		super(sender, target, message);
	}
}
