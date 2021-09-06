package dev.fulmineo.guild.data;

import java.util.Random;

public class Range {
	public Integer min;
	public Integer max;

	private void init() {
		if (this.min == null) this.min = 1;
		if (this.max == null) this.max = 64;
	}

	public boolean contains(int value) {
		this.init();
		return value >= this.min && value <= this.max;
	}

	public int getQuantityInRange() {
		this.init();
		int val = (new Random()).nextInt(max - min) + min + 1;
		return val;
	}
}
