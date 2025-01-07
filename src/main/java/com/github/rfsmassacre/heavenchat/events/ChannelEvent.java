package com.github.rfsmassacre.heavenchat.events;

import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class ChannelEvent extends Event implements Cancellable
{
    //Handler List
    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return HANDLERS;
    }
    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    private final ChannelMember member;
    private final Channel channel;
    private boolean cancel;

    public ChannelEvent(ChannelMember member, Channel channel)
    {
        this.member = member;
        this.channel = channel;
        this.cancel = false;
    }

    @Override
    public boolean isCancelled()
    {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        this.cancel = cancel;
    }
}
