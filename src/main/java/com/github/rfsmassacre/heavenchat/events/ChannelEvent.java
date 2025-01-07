package com.github.rfsmassacre.heavenchat2.events;

import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.ResultedEvent.GenericResult;
import lombok.Getter;

public abstract class ChannelEvent implements ResultedEvent<GenericResult>
{
    @Getter
    private final ChannelMember member;
    @Getter
    private final Channel channel;

    private GenericResult result = GenericResult.allowed(); // Allowed by default

    public ChannelEvent(ChannelMember member, Channel channel)
    {
        this.member = member;
        this.channel = channel;
    }

    @Override
    public GenericResult getResult()
    {
        return result;
    }

    @Override
    public void setResult(GenericResult result)
    {
        this.result = result;
    }
}
