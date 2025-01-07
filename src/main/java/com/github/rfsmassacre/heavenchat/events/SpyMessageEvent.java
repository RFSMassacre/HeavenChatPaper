package com.github.rfsmassacre.heavenchat2.events;

import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import lombok.Getter;

public class SpyMessageEvent extends MessageEvent
{
	@Getter
	private final ChannelMember spy;

	public SpyMessageEvent(ChannelMember sender, ChannelMember target, ChannelMember spy, String message)
	{
		super(sender, target, message);

		this.spy = spy;
	}
}
