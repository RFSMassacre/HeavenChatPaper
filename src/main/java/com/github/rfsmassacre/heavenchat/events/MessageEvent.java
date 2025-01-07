package com.github.rfsmassacre.heavenchat.events;

import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MessageEvent extends Event implements Cancellable
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

    @Getter
    private final ChannelMember sender;
    @Getter
    private final ChannelMember target;
    @Getter
    @Setter
    private String message;
    private boolean cancel;

    public MessageEvent(ChannelMember sender, ChannelMember target, String message)
    {
        this.sender = sender;
        this.target = target;
        this.message = message;
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
