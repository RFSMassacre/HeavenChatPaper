package com.github.rfsmassacre.heavenchat.events;

import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;

public class ChannelJoinEvent extends ChannelEvent
{
	public ChannelJoinEvent(ChannelMember member, Channel channel)
	{
		super(member, channel);
	}
}
