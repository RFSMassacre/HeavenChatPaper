package com.github.rfsmassacre.heavenchat.events;

import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import lombok.Getter;
import lombok.Setter;

public class ChannelMessageEvent extends ChannelEvent
{
	@Getter
	@Setter
	private String message;
	
	public ChannelMessageEvent(ChannelMember member, Channel channel, String message)
	{
		super(member, channel);

		this.message = message;
	}
}
