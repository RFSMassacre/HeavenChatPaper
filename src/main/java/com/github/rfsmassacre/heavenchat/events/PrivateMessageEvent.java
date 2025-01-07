package com.github.rfsmassacre.heavenchat.events;

import com.github.rfsmassacre.heavenchat.players.ChannelMember;

public class PrivateMessageEvent extends MessageEvent
{
	public PrivateMessageEvent(ChannelMember sender, ChannelMember target, String message)
	{
		super(sender, target, message);
	}
}
