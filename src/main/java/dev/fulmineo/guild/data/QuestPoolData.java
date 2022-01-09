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
		Integer max = this.getMax();
		return max != null ? Math.min(max, worth / unitWorth) : worth / unitWorth;
	}

	public Integer getMax() {
		if (this.number.max != null) {
			return this.number.max;
		} else if (this.type == "item") {
			return Registry.ITEM.get(new Identifier(this.name)).getMaxCount();
		} else {
			return null;
		}
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
