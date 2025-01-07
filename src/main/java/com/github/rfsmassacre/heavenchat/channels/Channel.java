package com.github.rfsmassacre.heavenchat2.channels;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.commands.FocusCommand;
import com.github.rfsmassacre.heavenchat2.data.ChannelGson;
import com.github.rfsmassacre.heavenchat2.events.ChannelMessageEvent;
import com.github.rfsmassacre.heavenchat2.library.configs.Locale;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Channel
{
    private static final Map<String, Channel> CHANNELS = new HashMap<>();
    private static final ChannelGson DATA = new ChannelGson();

    public static void addChannel(Channel channel)
    {
        CHANNELS.put(channel.name, channel);
    }

    public static void removeChannel(String channelName)
    {
        CHANNELS.remove(channelName);
    }

    public static Channel getChannel(String channelName)
    {
        return CHANNELS.get(channelName);
    }

    public static Set<Channel> getChannels()
    {
        return new HashSet<>(CHANNELS.values());
    }

    public static void clearChannels()
    {
        CHANNELS.clear();
    }

    public static ChannelGson getData()
    {
        return DATA;
    }

    public enum ChannelType
    {
        GLOBAL,
        SERVER,
        LOCAL
    }

    public static void loadAllChannels()
    {
        DATA.allAsync((channels) ->
        {
            for (Channel channel : Channel.getChannels())
            {
                HeavenChat.getInstance().getServer().getCommandManager().unregister(channel.getShortcut());
            }

            clearChannels();
            if (channels.size() == 0)
            {
                //DEFAULT CHANNELS
                Channel global = new Channel(ChannelType.GLOBAL, "global", "&eGlobal Channel");
                global.setFormat("&6[&eG&6] &f%luckperms_prefix%&f%player_displayname%&r&6:&f {message}");
                global.setShortcut("g");
                global.registerShortcut();

                Channel staff = new Channel(ChannelType.GLOBAL, "staff", "&bStaff Channel");
                staff.setFormat("&3[&bSC&3] &f%luckperms_prefix%&f%player_displayname%&r&3:&b {message}");
                staff.setShortcut("sc");
                staff.registerShortcut();

                Channel server = new Channel(ChannelType.SERVER, "server","&aServer Channel");
                server.setFormat("&7[&2S&7] &f%luckperms_prefix%&f%player_displayname%&r&2:&f {message}");
                server.setShortcut("s");
                server.registerShortcut();

                Channel local = new Channel(ChannelType.LOCAL, "local", "&7Local Channel");
                local.setFormat("&7[&fL&7] &f%luckperms_prefix%&f%player_displayname%&r&7:&f {message}");
                local.setShortcut("l");
                local.registerShortcut();

                addChannel(global);
                addChannel(staff);
                addChannel(server);
                addChannel(local);

                HeavenChat.getInstance().getServer().getScheduler().buildTask(HeavenChat.getInstance(), () ->
                {
                    DATA.write(global.getName(), global);
                    DATA.write(staff.getName(), staff);
                    DATA.write(server.getName(), server);
                    DATA.write(local.getName(), local);
                }).delay(0L, TimeUnit.SECONDS).schedule();

                return;
            }

            for (Channel channel : channels)
            {
                channel.registerShortcut();
                addChannel(channel);
            }
        });
    }

    public static void saveAllChannels()
    {
        HeavenChat.getInstance().getServer().getScheduler().buildTask(HeavenChat.getInstance(), () ->
        {
            for (Channel channel : CHANNELS.values())
            {
                DATA.write(channel.name, channel);
            }
        }).delay(0L, TimeUnit.SECONDS).schedule();
    }

    @Getter
    private final ChannelType channelType;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String displayName;
    private final Set<UUID> memberIds;
    @Getter
    @Setter
    private String format; //Format of the text
    @Getter
    @Setter
    private String shortcut;

    public Channel(ChannelType channelType, String name, String displayName)
    {
        this.channelType = channelType;
        this.name = name;
        this.displayName = displayName;
        this.memberIds = new HashSet<>();
        this.shortcut = "";
    }

    public void addMemberIds(UUID... playerIds)
    {
        memberIds.addAll(List.of(playerIds));
    }

    public void removeMemberId(UUID playerId)
    {
        memberIds.remove(playerId);
    }

    public boolean isMemberId(UUID playerId)
    {
        return memberIds.contains(playerId);
    }

    public Set<UUID> getMemberIds()
    {
        return new HashSet<>(memberIds);
    }

    public Set<ChannelMember> getMembers()
    {
        Set<ChannelMember> members = new HashSet<>();
        for (UUID memberId : memberIds)
        {
            ChannelMember member = ChannelMember.getMember(memberId);
            if (member != null)
            {
                members.add(member);
            }
        }

        return members;
    }

    public ChannelMember findMember(String name)
    {
        for (UUID memberId : memberIds)
        {
            ChannelMember member = ChannelMember.getMember(memberId);
            if (member != null && member.getName().equals(name))
            {
                return member;
            }
        }

        return null;
    }

    /*
     * Functions
     */
    private void sendGlobalMessage(ChannelMember sender, String message)
    {
        for (ChannelMember member : this.getMembers())
        {
            member.sendChannelMessage(this, sender, message);
        }
    }

    private void sendServerMessage(ChannelMember sender, String message)
    {
        for (ChannelMember member : this.getMembers())
        {
            Player player = member.getPlayer();
            Optional<ServerConnection> memberServer = player.getCurrentServer();
            Optional<ServerConnection> senderServer = sender.getPlayer().getCurrentServer();
            if (memberServer.isPresent() && senderServer.isPresent())
            {
                String memberServerName = memberServer.get().getServer().getServerInfo().getName();
                String senderServerName = senderServer.get().getServer().getServerInfo().getName();
                if (memberServerName.equals(senderServerName))
                {
                    member.sendChannelMessage(this, sender, message);
                }
            }
        }
    }

    private void sendLocalMessage(ChannelMember sender, String message)
    {
        HeavenChat.getInstance().getPapi().formatPlaceholders("%proximity_nearby%", sender.getPlayerId())
                .thenAccept((nearby) ->
        {
            if (nearby.isEmpty())
            {
                Locale locale = HeavenChat.getInstance().getLocale();
                locale.sendLocale(sender.getPlayer(), "channel.no-proximity");
                return;
            }

            List<UUID> nearbyIds = new ArrayList<>();
            for (String playerIdString : nearby.split(":"))
            {
                nearbyIds.add(UUID.fromString(playerIdString));
            }

            for (UUID playerId : nearbyIds)
            {
                ChannelMember member = ChannelMember.getMember(playerId);
                if (member != null && this.memberIds.contains(playerId))
                {
                    member.sendChannelMessage(this, sender, message);
                }
            }

            sender.sendChannelMessage(this, sender, message);
        });
    }

    public void sendMessage(ChannelMember sender, String message)
    {
        //Does not send if another plugin cancels this event
        switch (channelType)
        {
            case GLOBAL -> sendGlobalMessage(sender, message);
            case SERVER -> sendServerMessage(sender, message);
            case LOCAL -> sendLocalMessage(sender, message);
        }
    }

    public boolean canJoin(ChannelMember member)
    {
        return member.getPlayer().hasPermission("heavenchat.channel." + name);
    }
    public boolean canLeave(ChannelMember member)
    {
        return member.getPlayer().hasPermission("heavenchat.leave." + name);
    }

    public void registerShortcut()
    {
        CommandManager commandManager = HeavenChat.getInstance().getServer().getCommandManager();
        commandManager.register(shortcut, new FocusCommand(this));
    }
}
