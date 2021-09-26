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

import dev.fulmineo.guild.Guild;
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

	public static Quest create(QuestProfession profession, PlayerEntity player) {
		int level = QuestHelper.getCurrentLevel(ServerDataManager.levels.get(profession.levelsPool), ((GuildServerPlayerEntity)player).getProfessionExp(profession.name));

		List<QuestPoolData> tasks = new ArrayList<>(profession.tasks);
		Iterator<QuestPoolData> iterator = tasks.iterator();
		while (iterator.hasNext()) {
			if (!iterator.next().isAvailableFor(level+1)) iterator.remove();
		}
		tasks = WeightedItemHelper.getWeightedItems(tasks, (new Random()).nextInt(MAX_TASK_ROLLS) + 1);

		NbtList itemList = new NbtList();
		NbtList entityList = new NbtList();
		NbtList cureList = new NbtList();
		NbtList summonList = new NbtList();
		NbtList rewardList = new NbtList();

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
				qpd.number.min += task.number.min;
				qpd.number.max += task.number.max;
			}
		}

		for (QuestPoolData task : groupedTasks.values()) {
			NbtCompound nbt = new NbtCompound();
			nbt.putString("Name", task.name);
			if (task.icon != null) nbt.putString("Icon", task.icon);
			int needed = task.getQuantityInRange();
			nbt.putInt("Needed", needed);
			time += needed * task.unitTime;
			worth += needed * task.unitWorth;
			exp += needed * task.unitExp;
			switch (task.type) {
				case "item": {
					itemList.add(nbt);
					break;
				}
				case "entity": {
					entityList.add(nbt);
					break;
				}
				case "cure": {
					cureList.add(nbt);
					break;
				}
				case "summon": {
					summonList.add(nbt);
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
		iterator = rewardsCopy.iterator();
		while (iterator.hasNext()) {
			QuestPoolData reward = iterator.next();
			if (!reward.isAvailableFor(level+1) || reward.getMinWorth() > worth) iterator.remove();
		}

		List<QuestPoolData> primaryRewards = WeightedItemHelper.getWeightedItems(rewardsCopy, 6 - MAX_TASK_ROLLS);
		primaryRewards.sort(new MinWeightComparator());
		for (QuestPoolData reward : primaryRewards){
			if (reward.getMinWorth() <= worth) {
				NbtCompound nbt = new NbtCompound();
				nbt.putString("Name", reward.name);
				int count = reward.getCountByWorth(worth);
				nbt.putInt("Count", count);
				rewardList.add(nbt);
				worth -= reward.unitWorth * count;
			}
			rewardsCopy.remove(reward);
		}
		Collections.reverse(rewardList);

		NbtCompound nbt = new NbtCompound();
		nbt.putString("Profession", profession.name);
		if (time > 0) {
			nbt.putInt("Time", time);
		}
		if (Guild.EXPIRATION_TICKS != 0) {
			nbt.putLong("AvailableUntil", player.world.getTime() + Guild.EXPIRATION_TICKS);
		}
		nbt.putInt("Exp", exp);
		nbt.put("Entity", entityList);
		nbt.put("Cure", cureList);
		nbt.put("Summon", summonList);
		nbt.put("Item", itemList);
		nbt.put("Reward", rewardList);
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

	public NbtList getItemList() {
		return this.nbt.getList("Item", NbtElement.COMPOUND_TYPE);
	}

	public NbtList getEntityList() {
		return this.nbt.getList("Entity", NbtElement.COMPOUND_TYPE);
	}

	public NbtList getCureList() {
		return this.nbt.getList("Cure", NbtElement.COMPOUND_TYPE);
	}

	public NbtList getSummonList() {
		return this.nbt.getList("Summon", NbtElement.COMPOUND_TYPE);
	}

	public NbtList getRewardList() {
		return this.nbt.getList("Reward", NbtElement.COMPOUND_TYPE);
	}

	public String getRemainingTime(long currentTime) {
		int seconds;
		if (this.nbt.contains("ExpiresAt")) {
			seconds = (int)((this.nbt.getLong("ExpiresAt") - currentTime) / 20);
			if (seconds <= 0) return "00:00";
		} else {
			seconds = this.nbt.getInt("Time");
			if (seconds == 0) return "";
		}
		return this.timeToString(seconds);
	}

	public String getAcceptationTime(long currentTime) {
		int seconds;
		if (this.nbt.contains("AvailableUntil")) {
			seconds = (int)((this.nbt.getLong("AvailableUntil") - currentTime) / 20);
			if (seconds <= 0) return "00:00";
			return this.timeToString(seconds);
		}
		return "";
	}

	public String timeToString(int seconds) {
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
		this.nbt.remove("AvailableUntil");
	}

	public boolean tick() {
		if (this.nbt.contains("Time")) {
			int time = this.nbt.getInt("Time") - 1;
			this.nbt.putInt("Time", time);
			return time < 0;
		}
		return false;
	}

	public void updateEntity(String entityIdentifier, PlayerEntity player) {
		this.updateNbt("Entity", entityIdentifier, player);
	}

	public void updateCure(String entityIdentifier, PlayerEntity player) {
		this.updateNbt("Cure", entityIdentifier, player);
	}

	public void updateSummon(String entityIdentifier, PlayerEntity player) {
		this.updateNbt("Summon", entityIdentifier, player);
	}

	private void updateNbt(String listName, String identifier, PlayerEntity player) {
		if (this.nbt.contains(listName)) {
			NbtList entities = this.nbt.getList(listName, NbtElement.COMPOUND_TYPE);
			for (NbtElement elem : entities) {
				NbtCompound entry = (NbtCompound)elem;
				if (entry.getInt("Count") < entry.getInt("Needed") && entry.getString("Name").equals(identifier)) {
					entry.putInt("Count", entry.getInt("Count") + 1);
					break;
				}
			}
		}
	}

	public boolean isExpired(long time) {
		return this.nbt.getLong("ExpiresAt") < time;
	}

	public boolean tryComplete(ServerPlayerEntity player) {
		NbtList entities = this.nbt.getList("Entity", NbtElement.COMPOUND_TYPE);
		for (NbtElement elem : entities) {
			NbtCompound entry = (NbtCompound)elem;
			if (entry.getInt("Count") != entry.getInt("Needed")) return false;
		}
		List<List<ItemStack>> stacksByIndex = new ArrayList<>();
		PlayerInventory inventory = player.getInventory();
		ImmutableList<DefaultedList<ItemStack>> mainAndOffhand = ImmutableList.of(inventory.main, inventory.offHand);
		NbtList items = this.nbt.getList("Item", NbtElement.COMPOUND_TYPE);
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
		QuestProfession profession = ServerDataManager.professions.get(professionName);
		if (profession != null) {
			List<QuestLevel> levels = ServerDataManager.levels.get(profession.levelsPool);
			int exp = guildPlayer.getProfessionExp(professionName);
			exp += this.nbt.getInt("Exp");
			QuestLevel lastLevel = levels.get(levels.size()-1);
			if (exp > lastLevel.exp) {
				exp = lastLevel.exp;
			}
			guildPlayer.setProfessionExp(professionName, exp);
		}
		this.giveRewards(player);
		return true;
	}

	public void giveRewards(ServerPlayerEntity player) {
		boolean expired = this.isExpired(player.world.getTime());
		NbtList rewards = this.getRewardList();
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
			ItemStack stack = new ItemStack(Registry.ITEM.get(new Identifier(reward.getString("Name"))), count);
			if (!player.giveItemStack(stack)) {
				player.dropItem(stack, false);
			}
		}
	}

	static class MinWeightComparator implements Comparator<QuestPoolData> {
        public int compare(QuestPoolData a, QuestPoolData b) {
			return b.getMinWorth() - a.getMinWorth();
        }
    }
}
