package com.github.rfsmassacre.heavenchat.events;

import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
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
