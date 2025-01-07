package com.github.rfsmassacre.heavenchat.players;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.channels.Channel;
import com.github.rfsmassacre.heavenchat.data.MemberGson;
import com.github.rfsmassacre.heavenchat.events.PrivateMessageEvent;
import com.github.rfsmassacre.heavenchat.events.SpyMessageEvent;
import com.github.rfsmassacre.heavenchat.utils.SwearUtils;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
@Setter
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

    private final UUID playerId;
    private String name;
    private String displayName;
    private UUID focusedMemberId;
    private UUID lastMemberId;
    private String focusedChannelName;
    private final Set<UUID> ignoredPlayerIds;
    private boolean spying;
    private boolean filtered;
    private final Set<String> blockedWords;

    public ChannelMember(Player player)
    {
        this.playerId = player.getUniqueId();
        this.name = player.getName();
        this.displayName = player.getDisplayName();
        this.ignoredPlayerIds = new HashSet<>();
        this.spying = false;
        this.filtered = true;
        this.blockedWords = new HashSet<>(HeavenChat.getInstance().getConfiguration()
                .getStringList("blocked-words"));
    }

    public Player getPlayer()
    {
        return HeavenChat.getInstance().getServer().getPlayer(playerId);
    }

    public String getDisplayName()
    {
        Player player = getPlayer();
        if (player != null)
        {
            this.displayName = player.getDisplayName();
        }

        return displayName != null && !displayName.isEmpty() ? displayName : name;
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(HeavenChat.getInstance(), () ->
        {
            if (hasIgnored(sender))
            {
                return;
            }

            PaperLocale locale = HeavenChat.getInstance().getLocale();
            String format = channel.getFormat();
            if (!getPlayerId().equals(sender.getPlayerId()))
            {
                locale.sendMessage(getPlayer(), false, format, "{sender}", sender.getDisplayName(),
                        "{message}", (filtered ? SwearUtils.censorSwears(message, blockedWords) : message));
            }
            else
            {
                locale.sendMessage(getPlayer(), false, format, "{sender}",
                        getDisplayName(), "{message}", (filtered ? SwearUtils.censorSwears(message, blockedWords) :
                                message));
            }
        });
    }

    public void sendPrivateMessage(ChannelMember target, String message)
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(HeavenChat.getInstance(), () ->
        {
            PrivateMessageEvent event = new PrivateMessageEvent(this, target, message);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
            {
                return;
            }

            if (hasIgnored(target))
            {
                return;
            }

            PaperConfiguration config = HeavenChat.getInstance().getConfiguration();
            PaperLocale locale = HeavenChat.getInstance().getLocale();
            String pmSend = config.getString("pm-formats.pm-send");
            String pmReceive = config.getString("pm-formats.pm-receive");
            locale.sendMessage(event.getSender().getPlayer(), false,
                    pmReceive, "{sender}", event.getSender().getDisplayName(), "{receiver}",
                    event.getTarget().getDisplayName(), "{message}", event.getMessage());
            locale.sendMessage(event.getTarget().getPlayer(), false, pmSend, "{sender}",
                    event.getSender().getDisplayName(), "{receiver}", event.getTarget().getDisplayName(), "{message}",
                    (filtered ? SwearUtils.censorSwears(event.getMessage(), blockedWords) : event.getMessage()));
            setLastMember(event.getTarget());
        });
    }

    public void sendSpyMessage(ChannelMember sender, ChannelMember receiver, String message)
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(HeavenChat.getInstance(), () ->
        {
            SpyMessageEvent event = new SpyMessageEvent(sender, receiver, this, message);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
            {
                return;
            }

            if (getPlayer().hasPermission("heavenchat.socialspy"))
            {
                PaperConfiguration config = HeavenChat.getInstance().getConfiguration();
                PaperLocale locale = HeavenChat.getInstance().getLocale();
                String pmSpy = config.getString("pm-formats.pm-spy");
                locale.sendMessage(event.getSpy().getPlayer(), false, pmSpy, "{sender}",
                        event.getSender().getDisplayName(), "{receiver}", event.getTarget().getDisplayName(),
                        "{message}", event.getMessage());
            }
        });
    }
}
