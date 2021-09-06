package dev.fulmineo.guild.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class QuestPoolData implements WeightedItem {
	public String type;
	public String name;
	public Range level;
	public Range number;
	public int unitWorth;
	public int unitTime;
	public int unitExp;
	public int weight;

	public int getWeight() {
		return this.weight;
	}

	public NbtCompound saveNbt(NbtCompound nbt) {
		nbt.putString("Name", "minecraft:creeper");
		nbt.putInt("Needed", 5);
		return nbt;
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
			case "entity": {
				if (Registry.ENTITY_TYPE.containsId(new Identifier(name))) return "";
			}
		}
		return name;
	}
}
