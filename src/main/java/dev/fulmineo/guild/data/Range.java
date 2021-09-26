package dev.fulmineo.guild.data;

import java.util.Random;

public class Range {
	public Integer min;
	public Integer max;

	public boolean contains(int value) {
		return (this.min == null || value >= this.min) && (this.max == null || value <= this.max);
	}

	public int getQuantityInRange() {
		int qmin = this.min == null ? 1 : this.min;
		int qmax = this.max == null ? 64 : this.max;
		int diff = qmax - qmin;
		int val = (diff > 0 ? (new Random()).nextInt(diff + 1) : 0) + qmin;
		return val;
	}
}
