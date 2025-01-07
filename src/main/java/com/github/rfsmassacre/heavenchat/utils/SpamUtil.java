package com.github.rfsmassacre.heavenchat.utils;

import com.github.rfsmassacre.heavenchat.HeavenChat;
import com.github.rfsmassacre.heavenchat.players.ChannelMember;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpamUtil
{
    //Number of milliseconds in these quantities
    private static final int MINUTE = 60000;
    private static final int SECOND = 1000;
    private static final Map<UUID, Message> MESSAGES = new HashMap<>();

    public static void setLastMessage(UUID playerId, String message)
    {
        MESSAGES.put(playerId, new Message(System.currentTimeMillis(), message));
    }

    /*
     * Class to hold both the message and time in one place.
     */
    private static class Message
    {
        public Long time;
        public String message;

        public Message(Long time, String message)
        {
            this.time = time;
            this.message = message;
        }
    }

    /*
     * Hard Filters - Meant to cancel the message completely
     */
    public static String getFloodSpamTime(ChannelMember member)
    {
        PaperConfiguration config = HeavenChat.getInstance().getConfiguration();
        Message message = MESSAGES.get(member.getPlayerId());
        if (config.getBoolean("filters.flood-filter.enabled") && message != null)
        {
            int cooldown = config.getInt("filters.flood-filter.message-cooldown") * SECOND;
            double time = System.currentTimeMillis() - message.time;
            return String.format("%.1f", (cooldown - time) / SECOND);
        }

        return "0";
    }

    public static boolean isFloodSpam(ChannelMember member)
    {
        PaperConfiguration config = HeavenChat.getInstance().getConfiguration();
        Message message = MESSAGES.get(member.getPlayerId());
        if (config.getBoolean("filters.flood-filter.enabled") && message != null
                && !member.getPlayer().hasPermission("heavenchat.spam.flood"))
        {
            int cooldown = config.getInt("filters.flood-filter.message-cooldown") * SECOND;
            long time = System.currentTimeMillis() - message.time;
            return cooldown - time <= cooldown && cooldown - time >= 0;
        }

        return false;
    }

    private static int getHammingDistance(String message, String target)
    {
        int hamming = 0;
        for (int index = 0; index < message.length() && index < target.length(); index++)
        {
            if (message.charAt(index) != target.charAt(index))
                hamming++;
        }

        return hamming + (message.length() > target.length() ? message.length() - target.length() : target.length() -
                message.length());
    }

    public static boolean isRepetitionSpam(ChannelMember member, String message)
    {
        PaperConfiguration config = HeavenChat.getInstance().getConfiguration();
        if (config.getBoolean("filters.repetition-filter.enabled") && MESSAGES.get(member.getPlayerId()) != null
                && !member.getPlayer().hasPermission("heavenchat.spam.repetition"))
        {
            String lastMessage = MESSAGES.get(member.getPlayerId()).message.replaceAll(" ",
                    "").toLowerCase();
            String thisMessage = message.replaceAll(" ", "").toLowerCase();

            int levenshtein = LevenshteinDistance.getDefaultInstance().apply(lastMessage, thisMessage);
            int hamming = getHammingDistance(lastMessage, thisMessage);
            double limit = Math.max(lastMessage.length(), thisMessage.length());
            double percent = (1 - ((double) (hamming + levenshtein) / 2) / limit) * 100;
            double maxPercent = config.getInt("filters.repetition-filter.block-percent");
            return percent > 0 && percent >= maxPercent;
        }

        return false;
    }

    /*
     * Soft Filters - Meant to filter out the unwanted but continue
     * the message.
     */
    public static String filterSpam(ChannelMember member, String message)
    {
        String filtered = filterCapSpam(member, message);
        filtered = filterLengthSpam(member, filtered);
        return filtered;
    }

    private static String filterCharacterSpam(ChannelMember member, String message)
    {
        PaperConfiguration config = HeavenChat.getInstance().getConfiguration();
        if (config.getBoolean("filters.character-filter.enabled") &&
                !member.getPlayer().hasPermission("heavenchat.spam.character"))
        {
            return message.replaceAll("(.+?)\\1+", "$1$1");
        }

        return message;
    }

    private static String filterLengthSpam(ChannelMember member, String message)
    {
        PaperConfiguration config = HeavenChat.getInstance().getConfiguration();
        if (config.getBoolean("filters.word-length-filter.enabled") &&
                !member.getPlayer().hasPermission("heavenchat.spam.length"))
        {
            int limit = config.getInt("filters.word-length-filter.limit");

            String[] parts = message.split(" ");
            for (int part = 0; part < parts.length; part++)
            {
                if (parts[part].length() > limit)
                    parts[part] = parts[part].substring(0, limit);

            }

            return String.join(" ", parts);
        }

        return message;
    }

    private static String filterCapSpam(ChannelMember member, String message)
    {
        PaperConfiguration config = HeavenChat.getInstance().getConfiguration();
        if (config.getBoolean("filters.caps-filter.enabled") &&
                !member.getPlayer().hasPermission("heavenchat.spam.caps"))
        {
            int limit = config.getInt("filters.caps-filter.limit");

            int caps = 0;
            for (char character : message.toCharArray())
            {
                if (Character.isUpperCase(character))
                    caps++;
            }

            if (caps > limit)
            {
                String firstChar = message.substring(0, 1).toUpperCase();
                String lastChars = message.substring(1).toLowerCase();

                return firstChar + lastChars;
            }
        }

        return message;
    }
}
