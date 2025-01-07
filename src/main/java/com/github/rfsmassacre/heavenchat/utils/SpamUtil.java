package com.github.rfsmassacre.heavenchat2.utils;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.library.configs.Configuration;
import com.github.rfsmassacre.heavenchat2.players.ChannelMember;
import org.apache.commons.lang3.StringUtils;

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
        Configuration config = HeavenChat.getInstance().getConfiguration();
        if (config.getBoolean("filters.flood-filter.enabled"))
        {
            int cooldown = config.getInt("filters.flood-filter.message-cooldown") * SECOND;
            float time = (System.currentTimeMillis() - MESSAGES.get(member.getPlayerId()).time);
            float timeRemaining = cooldown - time;

            return String.format("%.1f", (timeRemaining / SECOND));
        }

        return "0";
    }

    public static boolean isFloodSpam(ChannelMember member)
    {
        Configuration config = HeavenChat.getInstance().getConfiguration();
        if (config.getBoolean("filters.flood-filter.enabled") && MESSAGES.get(member.getPlayerId()) != null
                && !member.getPlayer().hasPermission("heavenchat.spam.flood"))
        {
            int cooldown = config.getInt("filters.flood-filter.message-cooldown") * SECOND;
            long time = System.currentTimeMillis() - MESSAGES.get(member.getPlayerId()).time;
            return time <= cooldown;
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
        Configuration config = HeavenChat.getInstance().getConfiguration();
        if (config.getBoolean("filters.repetition-filter.enabled") && MESSAGES.get(member.getPlayerId()) != null
                && !member.getPlayer().hasPermission("heavenchat.spam.repetition"))
        {
            String lastMessage = MESSAGES.get(member.getPlayerId()).message.replaceAll(" ", "").toLowerCase();
            String thisMessage = message.replaceAll(" ", "").toLowerCase();

            double levenshtein = StringUtils.getLevenshteinDistance(lastMessage, thisMessage);
            double hamming = getHammingDistance(lastMessage, thisMessage);
            double limit = Math.max(lastMessage.length(), thisMessage.length());
            double percent = (1 - ((hamming + levenshtein) / 2) / limit) * 100;

            return percent >= config.getInt("filters.repetition-filter.block-percent");
        }

        return false;
    }

    /*
     * Soft Filters - Meant to filter out the unwanted but continue
     * the message.
     */
    public static String filterSpam(ChannelMember member, String message)
    {
        return filterCapSpam(member, filterLengthSpam(member, filterCharacterSpam(member, message)));
    }

    private static String filterCharacterSpam(ChannelMember member, String message)
    {
        Configuration config = HeavenChat.getInstance().getConfiguration();
        if (config.getBoolean("filters.character-filter.enabled") &&
                !member.getPlayer().hasPermission("heavenchat.spam.character"))
        {
            return message.replaceAll("(.+?)\\1+", "$1$1");
        }

        return message;
    }

    private static String filterLengthSpam(ChannelMember member, String message)
    {
        Configuration config = HeavenChat.getInstance().getConfiguration();
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
        Configuration config = HeavenChat.getInstance().getConfiguration();
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
