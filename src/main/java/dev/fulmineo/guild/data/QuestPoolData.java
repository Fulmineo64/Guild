package dev.fulmineo.guild.data;

import dev.fulmineo.guild.helper.WeightedItem;
import net.minecraft.nbt.NbtCompound;

public class QuestPoolData implements WeightedItem {
	public String type;
	public String name;
	public QuestPoolRange range;
	public int unitWorth;
	public int unitTime;
	public int weight;

	public int getWeight() {
		return this.weight;
	}

	public NbtCompound saveNbt(NbtCompound nbt) {
		nbt.putString("Name", "minecraft:creeper");
		nbt.putInt("Needed", 5);
		return nbt;
	}

	public int getQuantityInRange() {
		return this.range.getQuantityInRange();
	}

	public int getMinWorth() {
		return this.range.min * this.weight;
	}

	public int getCountByWorth(int worth) {
		return Math.min(range.max, worth / unitWorth);
	}
}
