package com.github.rfsmassacre.heavenchat2.events;

import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;

public class ChannelJoinEvent extends ChannelEvent
{
	public ChannelJoinEvent(ChannelMember member, Channel channel)
	{
		super(member, channel);
	}
}
