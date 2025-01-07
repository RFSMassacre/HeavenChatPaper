package com.github.rfsmassacre.heavenchat2.events;

import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import lombok.Getter;

public class ChannelLeaveEvent extends ChannelEvent
{
	@Getter
	private final boolean kicked;

	public ChannelLeaveEvent(ChannelMember member, Channel channel, boolean kicked)
	{
		super(member, channel);

		this.kicked = kicked;
	}
}
