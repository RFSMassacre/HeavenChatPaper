package com.github.rfsmassacre.heavenchat.channels;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.commands.FocusCommand;
import com.github.rfsmassacre.heavenchat.data.ChannelGson;
import com.github.rfsmassacre.heavenchat.events.ChannelMessageEvent;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenchat.utils.SpamUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

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
        LOCAL
    }

    public static void loadAllChannels()
    {
        DATA.allAsync((channels) ->
        {
            for (Channel channel : Channel.getChannels())
            {
                channel.unregisterShortcut();
            }

            clearChannels();
            if (channels.isEmpty())
            {
                //DEFAULT CHANNELS
                Channel global = new Channel(ChannelType.GLOBAL, "global", "&eGlobal Channel");
                global.setFormat("§6[§eG§6] §f{sender}§r§6:§f {message}");
                global.setShortcut("g");
                global.registerShortcut();

                Channel staff = new Channel(ChannelType.GLOBAL, "staff", "&bStaff Channel");
                staff.setFormat("§3[§bSC§3] §f{sender}§r§3:§b {message}");
                staff.setShortcut("sc");
                staff.registerShortcut();

                Channel local = new Channel(ChannelType.LOCAL, "local", "&7Local Channel");
                local.setFormat("§7[§fL§7] §f{sender}§r§7:§f {message}");
                local.setShortcut("l");
                local.registerShortcut();

                addChannel(global);
                addChannel(staff);
                addChannel(local);

                Bukkit.getScheduler().runTaskAsynchronously(HeavenChat.getInstance(), () ->
                {
                    DATA.write(global.getName(), global);
                    DATA.write(staff.getName(), staff);
                    DATA.write(local.getName(), local);
                });

                return;
            }

            for (Channel channel : channels)
            {
                channel.registerShortcut();
                addChannel(channel);
            }
        });
    }

    public static void saveAllChannels(boolean async)
    {
        if (async)
        {
            Bukkit.getScheduler().runTaskAsynchronously(HeavenChat.getInstance(), () ->
            {
                for (Channel channel : CHANNELS.values())
                {
                    DATA.write(channel.name, channel);
                }
            });
        }
        else
        {
            for (Channel channel : CHANNELS.values())
            {
                DATA.write(channel.name, channel);
            }
        }
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
    @Getter
    @Setter
    private String permission;

    public Channel(ChannelType channelType, String name, String displayName)
    {
        this.channelType = channelType;
        this.name = name;
        this.displayName = displayName;
        this.memberIds = new HashSet<>();
        this.shortcut = "";
        this.permission =  "heavenchat.channel." + name;
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

        SpamUtil.setLastMessage(sender.getPlayerId(), message);
    }

    private void sendLocalMessage(ChannelMember sender, String message)
    {
        int range = HeavenChat.getInstance().getConfiguration().getInt("local-range");
        for (Entity entity : sender.getPlayer().getNearbyEntities(range, range, range))
        {
            if (entity instanceof Player nearbyPlayer && !sender.getPlayerId().equals(nearbyPlayer.getUniqueId()))
            {
                ChannelMember member = ChannelMember.getMember(nearbyPlayer.getUniqueId());
                if (member != null && this.memberIds.contains(nearbyPlayer.getUniqueId()))
                {
                    member.sendChannelMessage(this, sender, message);
                }
            }
        }

        sender.sendChannelMessage(this, sender, message);
        SpamUtil.setLastMessage(sender.getPlayerId(), message);
    }

    public void sendMessage(ChannelMember sender, String message)
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(HeavenChat.getInstance(), () ->
        {
            //Does not send if another plugin cancels this event
            ChannelMessageEvent event = new ChannelMessageEvent(sender, this, message);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
            {
                return;
            }

            switch (channelType)
            {
                case GLOBAL ->
                {
                    sendGlobalMessage(sender, event.getMessage());
                }
                case LOCAL ->
                {
                    sendLocalMessage(sender, event.getMessage());
                }
            }
        });
    }

    public boolean canJoin(ChannelMember member)
    {
        Player player = member.getPlayer();
        return player != null && player.hasPermission("heavenchat.channel." + name);
    }
    public boolean canLeave(ChannelMember member)
    {
        Player player = member.getPlayer();
        return player != null && player.hasPermission("heavenchat.leave." + name);
    }

    public void registerShortcut()
    {
        Bukkit.getCommandMap().register(shortcut, new FocusCommand(this));
    }

    public void unregisterShortcut()
    {
        Bukkit.getCommandMap().getKnownCommands().remove(shortcut);
    }
}
