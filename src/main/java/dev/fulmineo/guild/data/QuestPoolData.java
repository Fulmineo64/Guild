package dev.fulmineo.guild.data;

import dev.fulmineo.guild.Guild;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class QuestPoolData implements WeightedItem {
	public String type;
	public String name;
	public String icon;
	public Range level;
	public Range number;
	public NbtCompound tag;
	public NbtCompound iconTag;
	public int unitWorth;
	public int unitTime;
	public int unitExp;
	public int weight;

	public int getWeight() {
		return this.weight;
	}

	public boolean isAvailableFor(int level) {
		if (this.level == null) return true;
		return this.level.contains(level);
	}

	public int getQuantityInRange() {
		return this.number.getQuantityInRange();
	}

	public int getMinWorth() {
		return this.number.min * this.unitWorth;
	}

	public int getCountByWorth(int worth) {
		return Math.min(number.max, worth / unitWorth);
	}

	public String validate() {
		switch (type) {
			case "item": {
				if (Registry.ITEM.containsId(new Identifier(name))) return "";
			}
			case "slay", "summon", "cure": {
				if (Registry.ENTITY_TYPE.containsId(new Identifier(name))) return "";
			}
			case "currency": {
				if (Guild.economyDependency != null) {
					return Guild.economyDependency.validateCurrency(name);
				} else {
					return name + " no economy installed";
				}
			}
		}
		return name;
	}
}
