package com.github.rfsmassacre.heavenchat.data;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.spigot.files.GsonManager;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class ChannelGson extends GsonManager<Channel>
{
    private class ChannelAdapterFactory implements TypeAdapterFactory
    {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type)
        {
            return null;
        }
    }

    /**
     * Constructor.
     */
    public ChannelGson()
    {
        super(HeavenChat.getInstance(), "channels", Channel.class);
    }
}
