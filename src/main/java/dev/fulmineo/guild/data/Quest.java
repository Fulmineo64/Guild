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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class Quest {
	public static int MAX_TASK_ROLLS = 3;
	protected NbtCompound nbt;
	@Environment(EnvType.CLIENT)
	public List<QuestData> tasks = new ArrayList<>();
	@Environment(EnvType.CLIENT)
	public List<QuestData> rewards = new ArrayList<>();

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
		NbtList slayList = new NbtList();
		NbtList cureList = new NbtList();
		NbtList summonList = new NbtList();
		NbtList rewardList = new NbtList();

		int time = 0;
		int worth = 0;
		float exp = 0;
		float playerExp = 0;

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
			if (task.icon != null) {
				nbt.putString("Icon", task.icon);
				if (task.iconTag != null) {
					nbt.put("IconTag", task.iconTag);
				}
			}
			if (task.tag != null) {
				nbt.put("Tag", task.tag);
			}
			int needed = task.getQuantityInRange();
			nbt.putInt("Needed", needed);
			time += needed * task.unitTime;
			worth += needed * task.unitWorth;
			exp += needed * task.unitExp;
			playerExp += needed * task.unitPlayerExp;
			switch (task.type) {
				case "item": {
					itemList.add(nbt);
					break;
				}
				case "slay": {
					slayList.add(nbt);
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
			playerExp = playerExp * (1 + (timeVariationPercentage * -1));
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
				nbt.putString("Type", reward.type);
				nbt.putString("Name", reward.name);
				if (reward.tag != null) {
					nbt.put("Tag", reward.tag);
				}
				if (reward.icon != null) {
					nbt.putString("Icon", reward.icon);
					if (reward.iconTag != null) {
						nbt.put("IconTag", reward.iconTag);
					}
				}
				int count = reward.getCountByWorth(worth);
				nbt.putInt("Count", count);
				if (reward.type.equals("currency") && Guild.economyDependency != null) {
					nbt.putString("Label", Guild.economyDependency.getCurrencyName(reward.name, count));
				}
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
		if (Guild.CONFIG.expirationTicks != 0) {
			nbt.putLong("AvailableUntil", player.world.getTime() + Guild.CONFIG.expirationTicks);
		}
		nbt.putInt("Exp", Math.round(exp));
		nbt.putInt("PlayerExp", Math.round(playerExp));
		nbt.put("Slay", slayList);
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
		// Migration code from v0.1.x to v0.2.0
		// TODO: Remove me after a while!
		if (nbt.contains("Entity")) {
			nbt.put("Slay", nbt.get("Entity"));
		}
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

	public NbtList getSlayList() {
		return this.nbt.getList("Slay", NbtElement.COMPOUND_TYPE);
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

	public void updateSlay(String entityIdentifier, LivingEntity entity, PlayerEntity player) {
		this.updateEntityNbt("Slay", entityIdentifier, entity, player);
	}

	public void updateCure(String entityIdentifier, LivingEntity entity, PlayerEntity player) {
		this.updateEntityNbt("Cure", entityIdentifier, entity, player);
	}

	public void updateSummon(String entityIdentifier, LivingEntity entity, PlayerEntity player) {
		this.updateEntityNbt("Summon", entityIdentifier, entity, player);
	}

	private void updateEntityNbt(String listName, String identifier, LivingEntity entity, PlayerEntity player) {
		if (this.nbt.contains(listName)) {
			NbtList entities = this.nbt.getList(listName, NbtElement.COMPOUND_TYPE);
			for (NbtElement elem : entities) {
				NbtCompound entry = (NbtCompound)elem;
				if (entry.getInt("Count") < entry.getInt("Needed") && entry.getString("Name").equals(identifier) && (!entry.contains("Tag") || matchesNbt(entity.writeNbt(new NbtCompound()), entry.getCompound("Tag")))) {
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
		NbtList entities = this.nbt.getList("Slay", NbtElement.COMPOUND_TYPE);
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
			Item item = Registries.ITEM.get(new Identifier(entry.getString("Name")));
			int needed = entry.getInt("Needed");
			Iterator<DefaultedList<ItemStack>> iterator = mainAndOffhand.iterator();
			while (needed > 0 && iterator.hasNext()) {
				DefaultedList<ItemStack> defaultedList = (DefaultedList<ItemStack>) iterator.next();
				for (int i = 0; i < defaultedList.size(); ++i) {
					ItemStack stack = defaultedList.get(i);
					if (stack.isOf(item) && (!entry.contains("Tag") || matchesNbt(stack.getOrCreateNbt(), entry.getCompound("Tag")))){
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
		// TODO: Remove me!
		Guild.info("added "+this.nbt.getInt("PlayerExp")+" exp");
		player.addExperience(this.nbt.getInt("PlayerExp"));
		return true;
	}

	public void giveRewards(ServerPlayerEntity player) {
		boolean expired = this.isExpired(player.world.getTime());
		NbtList rewards = this.getRewardList();
		for (NbtElement elm: rewards) {
			NbtCompound entry = (NbtCompound)elm;
			String type = entry.getString("Type");
			int count = entry.getInt("Count");
			if (expired) {
				int cnt1 = player.world.random.nextInt(count + 1);
				if (player.hasStatusEffect(StatusEffects.LUCK)) {
					int cnt2 = player.world.random.nextInt(count + 1);
					count = Math.max(cnt1, cnt2);
				} else {
					count = cnt1;
				}
			}
			switch (type) {
				case "currency": {
					if (Guild.economyDependency != null) {
						Guild.economyDependency.giveReward(count, entry, player);
					}
				}
				default: {
					ItemStack stack = new ItemStack(Registries.ITEM.get(new Identifier(entry.getString("Name"))), count);
					if (entry.contains("Tag")) {
						stack.setNbt(entry.getCompound("Tag"));
					}
					if (!player.giveItemStack(stack)) {
						player.dropItem(stack, false);
					}
				}
			}
		}
	}

	static class MinWeightComparator implements Comparator<QuestPoolData> {
        public int compare(QuestPoolData a, QuestPoolData b) {
			return b.getMinWorth() - a.getMinWorth();
        }
    }

	public static boolean matchesNbt(NbtCompound nbtToMatch, NbtCompound nbt) {
		for (String key: nbt.getKeys()) {
			NbtElement elm = nbtToMatch.get(key);
			if (elm == null || !elm.equals(nbt.get(key))) return false;
		}
		return true;
	}

	public class QuestData {
		public String icon;
		public ItemStack stack;
		public int count;
		public int needed;
	}

	@Environment(EnvType.CLIENT)
	private void addItemTask(String icon, NbtCompound entry) {
		ItemStack stack;
		if (entry.contains("Icon")) {
			stack = new ItemStack(Registries.ITEM.get(new Identifier(entry.getString("Icon"))));
			if (entry.contains("IconTag")) {
				stack.setNbt(entry.getCompound("IconTag"));
			}
		} else {
			stack = new ItemStack(Registries.ITEM.get(new Identifier(entry.getString("Name"))));
			if (entry.contains("Tag")) {
				stack.setNbt(entry.getCompound("Tag"));
			}
		}
		QuestData data = new QuestData();
		data.icon = icon;
		data.stack = stack;
		data.count = entry.getInt("Count");
		data.needed = entry.getInt("Needed");
		this.tasks.add(data);
	}

	@Environment(EnvType.CLIENT)
	private void addEntityTask(String icon, NbtCompound entry) {
		ItemStack stack;
		if (entry.contains("Icon")) {
			stack = new ItemStack(Registries.ITEM.get(new Identifier(entry.getString("Icon"))));
			if (entry.contains("IconTag")) {
				stack.setNbt(entry.getCompound("IconTag"));
			}
		} else {
			Item spawnEgg = Registries.ITEM.get(new Identifier(entry.getString("Name")+"_spawn_egg"));
			if (spawnEgg == Items.AIR) {
				stack = new ItemStack(Items.DIAMOND_SWORD);
			} else {
				stack = new ItemStack(spawnEgg);
				stack.setCustomName(Registries.ENTITY_TYPE.get(new Identifier(entry.getString("Name"))).getName());
			}
		}
		QuestData data = new QuestData();
		data.icon = icon;
		data.stack = stack;
		data.count = entry.getInt("Count");
		data.needed = entry.getInt("Needed");
		this.tasks.add(data);
	}

	@Environment(EnvType.CLIENT)
	private void addReward(NbtCompound entry) {
		ItemStack stack;
		if (entry.contains("Icon")) {
			stack = new ItemStack(Registries.ITEM.get(new Identifier(entry.getString("Icon"))));
			if (entry.contains("IconTag")) {
				stack.setNbt(entry.getCompound("IconTag"));
			}
		} else {
			stack = new ItemStack(Registries.ITEM.get(new Identifier(entry.getString("Name"))));
			if (entry.contains("Tag")) {
				stack.setNbt(entry.getCompound("Tag"));
			}
		}
		if (entry.contains("Label")) {
			stack.setCustomName(Text.literal(entry.getString("Label")));
		}
		QuestData data = new QuestData();
		data.stack = stack;
		data.count = entry.getInt("Count");
		this.rewards.add(data);
	}

	@Environment(EnvType.CLIENT)
	public void updateTasksAndRewards() {
		this.tasks.clear();
		this.rewards.clear();

		NbtList itemList = this.getItemList();
		for (NbtElement elm: itemList) {
			this.addItemTask("✉", (NbtCompound)elm);
		}

		NbtList slayList = this.getSlayList();
		for (NbtElement elm: slayList) {
			this.addEntityTask("🗡", (NbtCompound)elm);
		}

		NbtList cureList = this.getCureList();
		for (NbtElement elm: cureList) {
			this.addEntityTask("✙", (NbtCompound)elm);
		}

		NbtList summonList = this.getSummonList();
		for (NbtElement elm: summonList) {
			this.addEntityTask("✦", (NbtCompound)elm);
		}

		NbtList rewards = this.getRewardList();
		for (NbtElement elm: rewards) {
			this.addReward((NbtCompound)elm);
		}
	}
}
