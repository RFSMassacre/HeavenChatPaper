package com.github.rfsmassacre.heavenchat2.players;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.channels.Channel;
import com.github.rfsmassacre.heavenchat2.data.MemberGson;
import com.github.rfsmassacre.heavenchat2.events.ChannelMessageEvent;
import com.github.rfsmassacre.heavenchat2.events.PrivateMessageEvent;
import com.github.rfsmassacre.heavenchat2.events.SpyMessageEvent;
import com.github.rfsmassacre.heavenchat2.library.configs.Configuration;
import com.github.rfsmassacre.heavenchat2.library.configs.Locale;
import com.github.rfsmassacre.heavenchat2.utils.SwearUtils;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class ChannelMember
{
    private static final Map<UUID, ChannelMember> MEMBERS = new HashMap<>();
    private static final MemberGson DATA = new MemberGson();

    public static void addMember(ChannelMember member)
    {
        MEMBERS.put(member.playerId, member);
    }

    public static void removeMember(UUID playerId)
    {
        MEMBERS.remove(playerId);
    }

    public static ChannelMember getMember(UUID playerId)
    {
        return MEMBERS.get(playerId);
    }

    public static Set<ChannelMember> getMembers()
    {
        return new HashSet<>(MEMBERS.values());
    }

    public static ChannelMember findMember(String name)
    {
        for (ChannelMember member : getMembers())
        {
            if (member.getName().equals(name))
            {
                return member;
            }
        }

        return null;
    }

    public static MemberGson getData()
    {
        return DATA;
    }

    @Getter
    private final UUID playerId;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private UUID focusedMemberId;
    @Getter
    @Setter
    private UUID lastMemberId;
    @Getter
    @Setter
    private String focusedChannelName;
    private final Set<UUID> ignoredPlayerIds;
    @Getter
    @Setter
    private boolean spying;
    @Getter
    @Setter
    private boolean filtered;
    private final Set<String> blockedWords;

    public ChannelMember(Player player)
    {
        this.playerId = player.getUniqueId();
        this.name = player.getUsername();
        this.ignoredPlayerIds = new HashSet<>();
        this.spying = false;
        this.filtered = true;
        this.blockedWords = new HashSet<>(HeavenChat.getInstance().getConfiguration()
                .getStringList("blocked-words"));
    }

    public Player getPlayer()
    {
        Optional<Player> optional = HeavenChat.getInstance().getServer().getPlayer(playerId);
        return optional.orElse(null);
    }

    /*
     * Intended to be used for offline players as if they were online.
     */
    public ChannelMember getFocusedMember()
    {
        return focusedMemberId != null ? MEMBERS.get(focusedMemberId) : null;
    }

    public void setFocusedMember(ChannelMember focusedMember)
    {
        this.focusedMemberId = (focusedMember != null ? focusedMember.getPlayerId() : null);
    }

    public UUID getLastMemberId()
    {
        return lastMemberId;
    }

    public ChannelMember getLastMember()
    {
        return lastMemberId != null ? MEMBERS.get(lastMemberId) : null;
    }

    public void setLastMember(ChannelMember lastMember)
    {
        this.lastMemberId = lastMember != null ? lastMember.getPlayerId() : null;
    }

    //This is so all players retrieve the latest instance of that channel
    public Channel getFocusedChannel()
    {
        return focusedChannelName != null ? Channel.getChannel(focusedChannelName) : null;
    }

    public void setFocusedChannel(Channel focusedChannel)
    {
        this.focusedChannelName = focusedChannel != null ? focusedChannel.getName() : null;
    }

    public List<UUID> getIgnoredPlayerIds()
    {
        return new ArrayList<>(ignoredPlayerIds);
    }

    public boolean hasIgnored(ChannelMember member)
    {
        return ignoredPlayerIds.contains(member.getPlayerId());
    }

    public void addIgnoredMember(ChannelMember member)
    {
        ignoredPlayerIds.add(member.getPlayerId());
    }

    public void removeIgnoredMember(ChannelMember member)
    {
        ignoredPlayerIds.remove(member.getPlayerId());
    }

    public void blockWord(String word)
    {
        this.blockedWords.add(word);
    }

    public void unblockWord(String word)
    {
        this.blockedWords.remove(word);
    }

    public boolean isBlockedWord(String word)
    {
        return blockedWords.contains(word);
    }

    public Set<String> getBlockedWords()
    {
        return new HashSet<>(blockedWords);
    }

    /*
     * Functions
     */
    public void sendChannelMessage(Channel channel, ChannelMember sender, String message)
    {
        ChannelMessageEvent event = new ChannelMessageEvent(sender, channel, message);
        HeavenChat.getInstance().getServer().getEventManager().fire(event).thenAccept((action) ->
        {
            if (action.getResult().isAllowed() && !hasIgnored(action.getMember()))
            {
                Configuration config = HeavenChat.getInstance().getConfiguration();
                Locale locale = HeavenChat.getInstance().getLocale();
                String serverName = "default";
                Optional<ServerConnection> optional = action.getMember().getPlayer().getCurrentServer();
                if (optional.isPresent())
                {
                    serverName = optional.get().getServerInfo().getName();
                }

                String format = action.getChannel().getFormat();
                String serverPrefix = config.getString("servers." +  serverName + ".prefix");
                String server =  serverPrefix != null ? serverPrefix : "";
                String serverColor = config.getString("servers." + serverName + ".color");
                String color =  serverColor != null ? serverColor : "";
                locale.sendMessage(getPlayer(), action.getMember().getPlayer(), false, format, "{sender}",
                        action.getMember().getName(), "{server}", server, "{message}", (filtered ?
                                SwearUtils.censorSwears(action.getMessage(), blockedWords) : action.getMessage()),
                        "{color}", color);
            }
        });

    }

    public void sendPrivateMessage(ChannelMember target, String message)
    {
        PrivateMessageEvent event = new PrivateMessageEvent(this, target, message);
        HeavenChat.getInstance().getServer().getEventManager().fire(event).thenAccept((action) ->
        {
            if (action.getResult().isAllowed() && !hasIgnored(action.getTarget()))
            {
                Configuration config = HeavenChat.getInstance().getConfiguration();
                Locale locale = HeavenChat.getInstance().getLocale();
                String pmSend = config.getString("pm-formats.pm-send");
                String pmReceive = config.getString("pm-formats.pm-receive");
                locale.sendMessage(action.getSender().getPlayer(), action.getTarget().getPlayer(), false,
                        pmReceive, "{sender}", action.getSender().getName(), "{receiver}",
                        action.getTarget().getName(), "{message}", action.getMessage());
                locale.sendMessage(action.getTarget().getPlayer(), action.getSender().getPlayer(), false,
                        pmSend, "{sender}", action.getSender().getName(), "{receiver}",
                        action.getTarget().getName(), "{message}",
                        (filtered ? SwearUtils.censorSwears(action.getMessage(), blockedWords) : action.getMessage()));
                setLastMember(action.getTarget());
            }
        });

    }

    public void sendSpyMessage(ChannelMember sender, ChannelMember receiver, String message)
    {
        SpyMessageEvent event = new SpyMessageEvent(this, sender, receiver, message);
        HeavenChat.getInstance().getServer().getEventManager().fire(event).thenAccept((action) ->
        {
            if (action.getResult().isAllowed() && getPlayer().hasPermission("heavenchat.socialspy"))
            {
                Configuration config = HeavenChat.getInstance().getConfiguration();
                Locale locale = HeavenChat.getInstance().getLocale();
                String pmSpy = config.getString("pm-formats.pm-spy");
                locale.sendMessage(action.getSpy().getPlayer(), false, pmSpy, "{sender}",
                        action.getSender().getName(), "{receiver}", action.getTarget().getName(), "{message}",
                        action.getMessage());
            }
        });

    }
}
