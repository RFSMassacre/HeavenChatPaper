package com.github.rfsmassacre.heavenchat2.events;

import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import lombok.Getter;
import lombok.Setter;

public class ChannelMessageReceiveEvent extends ChannelMessageEvent
{
	@Getter
	@Setter
	private ChannelMember target;
	
	public ChannelMessageReceiveEvent(ChannelMember member, Channel channel, String message)
	{
		super(member, channel, message);
	}
}
