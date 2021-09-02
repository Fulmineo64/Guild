package dev.fulmineo.guild.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public class Quest {
	public static int MAX_TASK_ROLLS = 3;
	protected NbtCompound nbt;

	public Quest() {
		this.nbt = new NbtCompound();
	}

	public static Quest create(QuestProfession profession, long currentTime) {
		List<QuestPoolData> tasks = WeightedItemHelper.getWeightedItems(profession.tasks, (new Random()).nextInt(MAX_TASK_ROLLS) + 1);

		NbtList items = new NbtList();
		NbtList entities = new NbtList();
		NbtList rewards = new NbtList();

		int time = 0;
		int worth = 0;
		int exp = 0;

		Map<String, QuestPoolData> groupedTasks = new HashMap<>();
		for (QuestPoolData task : tasks) {
			String key = task.type+"_"+task.name;
			QuestPoolData qpd = groupedTasks.get(key);
			if (qpd == null) {
				groupedTasks.put(key, task);
			} else {
				qpd.range.min += task.range.min;
				qpd.range.max += task.range.max;
			}
		}

		for (QuestPoolData task : groupedTasks.values()) {
			NbtCompound nbt = new NbtCompound();
			nbt.putString("Name", task.name);
			int needed = task.getQuantityInRange();
			nbt.putInt("Needed", needed);
			time += needed * task.unitTime;
			worth += needed * task.unitWorth;
			exp += needed * task.unitExp;
			switch (task.type) {
				case "item": {
					items.add(nbt);
					break;
				}
				case "entity": {
					entities.add(nbt);
					break;
				}
			}
		}

		if (time > 0){
			int timeVariationPercentage = ((new Random()).nextInt(30) + 1 - 15) / 100;
			time = time * (1 + timeVariationPercentage);
			worth = worth * (1 + (timeVariationPercentage * -1));
			exp = exp * (1 + (timeVariationPercentage * -1));
		}

		List<QuestPoolData> rewardsCopy = new ArrayList<>(profession.rewards);
		Iterator<QuestPoolData> iterator = rewardsCopy.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getMinWorth() > worth) iterator.remove();
		}

		List<QuestPoolData> primaryRewards = WeightedItemHelper.getWeightedItems(rewardsCopy, 6 - MAX_TASK_ROLLS);
		primaryRewards.sort(new MinWeightComparator());
		for (QuestPoolData reward : primaryRewards){
			if (reward.getMinWorth() <= worth) {
				NbtCompound nbt = new NbtCompound();
				nbt.putString("Name", reward.name);
				int count = reward.getCountByWorth(worth);
				nbt.putInt("Count", count);
				rewards.add(nbt);
				worth -= reward.unitWorth * count;
			}
			rewardsCopy.remove(reward);
		}
		Collections.reverse(rewards);

		NbtCompound nbt = new NbtCompound();
		nbt.putString("Profession", profession.name);
		nbt.putInt("Time", time);
		nbt.putInt("Exp", exp);
		nbt.put("Entities", entities);
		nbt.put("Items", items);
		nbt.put("Rewards", rewards);
		return fromNbt(nbt);
	}

	public NbtCompound getNbt() {
		return this.nbt;
	}

	public static Quest fromNbt(NbtCompound nbt){
		Quest quest = new Quest();
		quest.nbt = nbt;
		return quest;
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		for (String key: this.nbt.getKeys()) {
			nbt.put(key, this.nbt.get(key));
		}
		return nbt;
	}


	public String getProfessionName() {
		return this.nbt.getString("Profession");
	}

	public NbtList getItems() {
		return this.nbt.getList("Items", NbtElement.COMPOUND_TYPE);
	}

	public NbtList getEntities() {
		return this.nbt.getList("Entities", NbtElement.COMPOUND_TYPE);
	}

	public NbtList getRewards() {
		return this.nbt.getList("Rewards", NbtElement.COMPOUND_TYPE);
	}

	public String getRemainingTime(long currentTime) {
		int seconds;
		if (this.nbt.contains("ExpiresAt")) {
			seconds = (int)((this.nbt.getLong("ExpiresAt") - currentTime) / 20);
			if (seconds == 0) return "00:00";
		} else {
			seconds = this.nbt.getInt("Time");
			if (seconds == 0) return "";
		}
		int minutes = seconds / 60;
		int hours = minutes / 60;
		seconds = seconds % 60;
		if (hours > 0){
			minutes = minutes % 60;
			return (hours > 9 ? "" : "0") + hours+":"+(minutes > 9 ? "" : "0") + minutes+":"+(seconds > 9 ? "" : "0")+seconds;
		} else {
			return (minutes > 9 ? "" : "0") + minutes+":"+(seconds > 9 ? "" : "0")+seconds;
		}
	}

	public void accept(long currentTime) {
		if (this.nbt.contains("Time")) {
			this.nbt.putLong("ExpiresAt", currentTime + this.nbt.getInt("Time") * 20);
		}
	}

	public boolean tick() {
		if (this.nbt.contains("Time")) {
			int time = this.nbt.getInt("Time") - 1;
			this.nbt.putInt("Time", time);
			return time < 0;
		}
		return false;
	}

	public void updateItems(ItemStack obtainedItemStack, PlayerEntity player) {
		if (this.nbt.contains("Items")/* && !this.nbt.getBoolean("Complete")*/) {
			NbtList items = this.nbt.getList("Items", NbtElement.COMPOUND_TYPE);
			String itemIdentifier = Registry.ITEM.getId(obtainedItemStack.getItem()).toString();
			for (NbtElement elem : items) {
				NbtCompound entry = (NbtCompound)elem;
				if (entry.getString("Item").equals(itemIdentifier) && entry.getInt("Count") < entry.getInt("Needed")) {
					int diff = entry.getInt("Needed") - entry.getInt("Count");
					if (diff > obtainedItemStack.getCount()) {
						diff = obtainedItemStack.getCount();
					}
					obtainedItemStack.decrement(diff);
					int updatedCount = entry.getInt("Count") + diff;
					entry.putInt("Count", updatedCount);
					/*if (updatedCount == entry.getInt("Needed")) {
						this.checkComplete(player);
					}*/
					break;
				}
			}
		}
	}

	public void updateEntities(String entityIdentifier, PlayerEntity player) {
		if (this.nbt.contains("Entities")/* && !this.nbt.getBoolean("Complete")*/) {
			NbtList entities = this.nbt.getList("Entities", NbtElement.COMPOUND_TYPE);
			for (NbtElement elem : entities) {
				NbtCompound entry = (NbtCompound)elem;
				if (entry.getInt("Count") < entry.getInt("Needed") && entry.getString("Name").equals(entityIdentifier)) {
					int updatedCount = entry.getInt("Count") + 1;
					entry.putInt("Count", updatedCount);
					/*if (updatedCount == entry.getInt("Needed")) {
						this.checkComplete(player);
					}*/
					break;
				}
			}
		}
	}

	public boolean isExpired(long time) {
		return this.nbt.getLong("ExpiresAt") < time;
	}

	public boolean tryComplete(ServerPlayerEntity player) {
		NbtList entities = this.nbt.getList("Entities", NbtElement.COMPOUND_TYPE);
		for (NbtElement elem : entities) {
			NbtCompound entry = (NbtCompound)elem;
			if (entry.getInt("Count") != entry.getInt("Needed")) return false;
		}
		List<List<ItemStack>> stacksByIndex = new ArrayList<>();
		PlayerInventory inventory = player.getInventory();
		ImmutableList<DefaultedList<ItemStack>> mainAndOffhand = ImmutableList.of(inventory.main, inventory.offHand);
		NbtList items = this.nbt.getList("Items", NbtElement.COMPOUND_TYPE);
		for (NbtElement elem: items) {
			NbtCompound entry = (NbtCompound)elem;
			List<ItemStack> stacks = new ArrayList<>();
			Item item = Registry.ITEM.get(new Identifier(entry.getString("Name")));
			int needed = entry.getInt("Needed");
			Iterator<DefaultedList<ItemStack>> iterator = mainAndOffhand.iterator();
			while (needed > 0 && iterator.hasNext()) {
				DefaultedList<ItemStack> defaultedList = (DefaultedList<ItemStack>) iterator.next();
				for (int i = 0; i < defaultedList.size(); ++i) {
					ItemStack stack = defaultedList.get(i);
					if (stack.isOf(item)){
						stacks.add(stack);
						needed -= stack.getCount();
						if (needed <= 0) break;
					}
				}
			}
			if (needed > 0) return false;
			stacksByIndex.add(stacks);
		}

		for (int i = 0; i < items.size(); i++) {
			NbtCompound entry = (NbtCompound)items.get(i);
			List<ItemStack> stacks = stacksByIndex.get(i);
			int needed = entry.getInt("Needed");
			Iterator<ItemStack> iterator = stacks.iterator();
			while (needed > 0 && iterator.hasNext()) {
				ItemStack stack = iterator.next();
				int count = stack.getCount();
				if (count <= needed) {
					needed -= count;
					stack.setCount(0);
				} else {
					stack.setCount(count - needed);
					needed = 0;
				}
			}
			if (needed > 0) return false;
		}

		GuildServerPlayerEntity guildPlayer = ((GuildServerPlayerEntity)player);
		String professionName = this.getProfessionName();
		QuestProfession profession = DataManager.professions.get(professionName);
		List<QuestLevel> levels = DataManager.levels.get(profession.levelsPool);
		int exp = guildPlayer.getProfessionExp(professionName);
		exp += this.nbt.getInt("Exp");
		QuestLevel lastLevel = levels.get(levels.size()-1);
		if (exp > lastLevel.exp) {
			exp = lastLevel.exp;
		}
		guildPlayer.setProfessionExp(professionName, exp);

		this.giveRewards(player);
		return true;
	}

	public void giveRewards(ServerPlayerEntity player) {
		boolean expired = this.isExpired(player.world.getTime());
		NbtList rewards = this.getRewards();
		for (NbtElement entry: rewards) {
			NbtCompound reward = (NbtCompound)entry;
			int count = reward.getInt("Count");
			if (expired) {
				int cnt1 = player.world.random.nextInt(count + 1);
				if (player.hasStatusEffect(StatusEffects.LUCK)) {
					int cnt2 = player.world.random.nextInt(count + 1);
					count = Math.max(cnt1, cnt2);
				} else {
					count = cnt1;
				}
			}
			player.giveItemStack(new ItemStack(Registry.ITEM.get(new Identifier(reward.getString("Name"))), count));
		}
	}

	/*private void checkComplete(PlayerEntity player) {
		NbtList entities = this.nbt.getList("Entities", NbtElement.COMPOUND_TYPE);
		for (NbtElement elem : entities) {
			NbtCompound entry = (NbtCompound)elem;
			if (entry.getInt("Count") != entry.getInt("Needed")) return;
		}
		NbtList items = this.nbt.getList("Items", NbtElement.COMPOUND_TYPE);
		for (NbtElement elem : items) {
			NbtCompound entry = (NbtCompound)elem;
			if (entry.getInt("Count") != entry.getInt("Needed")) return;
		}
		this.complete(player);
	}

	private void complete(PlayerEntity player) {
		this.nbt.putBoolean("Complete", true);
		player.sendMessage(new LiteralText("Quest completed"), true);
	}*/

	static class MinWeightComparator implements Comparator<QuestPoolData> {
        public int compare(QuestPoolData a, QuestPoolData b) {
			return b.getMinWorth() - a.getMinWorth();
        }
    }
}
