package com.github.rfsmassacre.heavenchat2.events;

import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.ResultedEvent.GenericResult;
import lombok.Getter;
import lombok.Setter;

public abstract class MessageEvent implements ResultedEvent<GenericResult>
{
    @Getter
    private final ChannelMember sender;
    @Getter
    private final ChannelMember target;
    @Getter
    @Setter
    private String message;

    private GenericResult result = GenericResult.allowed(); // Allowed by default

    public MessageEvent(ChannelMember sender, ChannelMember target, String message)
    {
        this.sender = sender;
        this.target = target;
        this.message = message;
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
