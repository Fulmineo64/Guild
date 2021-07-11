package dev.fulmineo.guild.data;

import java.util.Random;

public class QuestPoolRange {
	public int min;
	public int max;

	public int getQuantityInRange() {
		int val = (new Random()).nextInt(max - min) + min + 1;
		return val;
	}
}
