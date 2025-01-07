package com.github.rfsmassacre.heavenchat2.utils;

import com.github.rfsmassacre.heavenchat2.HeavenChat;
import com.github.rfsmassacre.heavenchat2.library.configs.Configuration;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class SwearUtils
{
	private static class Swear
	{
		public String swear;
		public int firstIndex;
		public int lastIndex;

		public Swear(String swear, int firstIndex, int lastIndex)
		{
			this.swear = swear;
			this.firstIndex = firstIndex;
			this.lastIndex = lastIndex;
		}
	}

	private static final Map<String, String> LEET_SPEAK = new HashMap<>();
	private static final DoubleMetaphone phone = new DoubleMetaphone();

	static
	{
		//Leet Speak definitions
		LEET_SPEAK.put("1", "i");
		LEET_SPEAK.put("!", "i");
		LEET_SPEAK.put("3", "e");
		LEET_SPEAK.put("4", "a");
		LEET_SPEAK.put("@", "a");
		LEET_SPEAK.put("5", "s");
		LEET_SPEAK.put("7", "t");
		LEET_SPEAK.put("0", "o");
		LEET_SPEAK.put("9", "g");
	}
	
	public static String fromLeetSpeak(String message)
	{
		String filteredMessage = message;
		for (String key : LEET_SPEAK.keySet())
		{
			filteredMessage = filteredMessage.replaceAll(key, LEET_SPEAK.get(key));
		}
		
		return filteredMessage;
	}

	public static String removeNonAlphabet(String message)
	{
		return message.replaceAll("[^A-Za-z ]", "");
	}

	public static String replaceSwear(String message, int firstIndex, int lastIndex)
	{
		Configuration config = HeavenChat.getInstance().getConfiguration();
		String stringCensor = config.getString("filters.profanity-filter.censor");
		char censor = (!stringCensor.equals("") ? stringCensor.charAt(0) : '*');
		
		char[] chars = message.toCharArray();
		for (int index = firstIndex; index < lastIndex + 1; index++)
		{
			if (chars[index] != ' ')
			{
				chars[index] = censor;
			}
		}
		
		return new String(chars);
	}
	
	public boolean containsSwear(String message, Set<String> swears)
	{
		Configuration config = HeavenChat.getInstance().getConfiguration();
		String rawMessage = removeNonAlphabet(fromLeetSpeak(message)).toLowerCase();
		for (Swear coord : extractIndexes(rawMessage, swears))
		{
			String possibleSwear = rawMessage.substring(coord.firstIndex, coord.lastIndex + 1)
					.replaceAll(" ", "");
			double percent = getPercentage(possibleSwear, coord.swear);
			if (phone.isDoubleMetaphoneEqual(possibleSwear, coord.swear) && percent >
					config.getInt("filters.profanity-filter.block-percent"))
			{
				return true;
			}
		}
		
		return false;
	}

	public static String censorSwears(String message, Set<String> swears)
	{
		Configuration config = HeavenChat.getInstance().getConfiguration();
		String rawMessage = fromLeetSpeak(message).toLowerCase();
		String finalMessage = message;
		for (Swear coord : extractIndexes(rawMessage, swears))
		{
			String possibleSwear = rawMessage.substring(coord.firstIndex, coord.lastIndex + 1);
			double percent = getPercentage(possibleSwear, coord.swear);
			
			if (phone.isDoubleMetaphoneEqual(removeNonAlphabet(possibleSwear).replace(" ", ""),
					coord.swear) && percent > config.getInt("filters.profanity-filter.block-percent"))
			{
				finalMessage = replaceSwear(finalMessage, coord.firstIndex, coord.lastIndex);
			}
		}
		
		return finalMessage;
	}
	private static List<Swear> extractIndexes(String message, Set<String> swears)
	{
		Configuration config = HeavenChat.getInstance().getConfiguration();
		String rawMessage = fromLeetSpeak(message);
		List<Swear> coords = new ArrayList<>();
		for (String swear : swears)
		{
			char firstChar = swear.charAt(0);
			char lastChar = swear.charAt(swear.length() - 1);
			
			int firstIndex = -1;
			int lastIndex = -1;
			
			for (int index = 0; index < rawMessage.length(); index++)
			{
				if (firstChar == rawMessage.charAt(index))
				{
					firstIndex = index;
				}
				
				if (lastChar == rawMessage.charAt(index) && index > firstIndex)
				{
					lastIndex = index;
					
					if (firstIndex != -1)
						coords.add(new Swear(swear, firstIndex, lastIndex));
				}
			}
				
		}
		
		return coords;
	}

	private static double getPercentage(String message, String swear)
	{
		double difference = StringUtils.getLevenshteinDistance(message, swear);
		double limit = (Math.max(swear.length(), message.length()));
		return (1 - (difference / limit)) * 100;
	}
}
