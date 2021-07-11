package dev.fulmineo.guild.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.QuestPoolData;
import dev.fulmineo.guild.data.QuestProfession;
import dev.fulmineo.guild.helper.WeightedItemHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class QuestScrollItem extends Item {
	public static int TASK_ROLLS = 2;

	public QuestScrollItem(Settings settings) {
		super(settings);
	}

	public static ItemStack create(QuestProfession profession) {
		ItemStack questScroll = new ItemStack(Guild.QUEST_SCROLL);
		NbtCompound tag = questScroll.getOrCreateTag();
		List<QuestPoolData> tasks = WeightedItemHelper.getWeightedItems(profession.tasks, TASK_ROLLS);

		NbtList items = new NbtList();
		NbtList entities = new NbtList();
		NbtList rewards = new NbtList();

		int time = 0;
		int worth = 0;

		for (QuestPoolData task : tasks) {
			NbtCompound nbt = new NbtCompound();
			nbt.putString("Name", task.name);
			int needed = task.getQuantityInRange();
			nbt.putInt("Needed", needed);
			time += needed * task.unitTime;
			worth += needed * task.unitWorth;
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
		}

		List<QuestPoolData> rewardsCopy = new ArrayList<>(profession.rewards);
		List<QuestPoolData> primaryRewards = WeightedItemHelper.getWeightedItems(rewardsCopy, TASK_ROLLS);
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

		int extraTries = Math.min(rewardsCopy.size(), 5);
		for (int i = 0; i < extraTries; i++) {
			QuestPoolData reward = WeightedItemHelper.getWeightedItems(rewardsCopy, 1).get(0);
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

		tag.putInt("Time", time);
		tag.put("Entities", entities);
		tag.put("Items", items);
		tag.put("Rewards", rewards);
		return questScroll;
	}

	public static void updateItems(ItemStack questItemStack, ItemStack obtainedItemStack, PlayerEntity player) {
		NbtCompound tag = questItemStack.getOrCreateTag();
		if (tag.contains("Items") && !tag.getBoolean("Complete")) {
			NbtList items = tag.getList("Items", NbtElement.COMPOUND_TYPE);
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
					if (updatedCount == entry.getInt("Needed")) {
						checkComplete(questItemStack, player);
					}
					break;
				}
			}
		}
	}

	public static void updateEntities(ItemStack questItemStack, LivingEntity killedEntity, PlayerEntity player) {
		NbtCompound tag = questItemStack.getOrCreateTag();
		if (tag.contains("Entities") && !tag.getBoolean("Complete")) {
			NbtList entities = tag.getList("Entities", NbtElement.COMPOUND_TYPE);
			String entityIdentifier = EntityType.getId(killedEntity.getType()).toString();
			for (NbtElement elem : entities) {
				NbtCompound entry = (NbtCompound)elem;
				if (entry.getString("Entity").equals(entityIdentifier) && entry.getInt("Count") < entry.getInt("Needed")) {
					int updatedCount = entry.getInt("Count") + 1;
					entry.putInt("Count", updatedCount);
					if (updatedCount == entry.getInt("Needed")) {
						checkComplete(questItemStack, player);
					}
					break;
				}
			}
		}
	}

	private static void checkComplete(ItemStack questItemStack, PlayerEntity player) {
		NbtCompound tag = questItemStack.getTag();
		NbtList entities = tag.getList("Entities", NbtElement.COMPOUND_TYPE);
		for (NbtElement elem : entities) {
			NbtCompound entry = (NbtCompound)elem;
			if (entry.getInt("Count") != entry.getInt("Needed")) return;
		}
		NbtList items = tag.getList("Items", NbtElement.COMPOUND_TYPE);
		for (NbtElement elem : items) {
			NbtCompound entry = (NbtCompound)elem;
			if (entry.getInt("Count") != entry.getInt("Needed")) return;
		}
		complete(questItemStack, player);
	}

	private static void complete(ItemStack questItemStack, PlayerEntity player) {
		NbtCompound tag = questItemStack.getTag();
		tag.putBoolean("Complete", true);
		player.sendMessage(new TranslatableText("test"), false);
	}

	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		NbtCompound tag = stack.getTag();
		if (tag == null) return;
		tooltip.add(new TranslatableText("item.guild.quest_scroll.tasks").formatted(Formatting.BLUE));
		if (tag.contains("Items")) {
			NbtList bounties = tag.getList("Items", NbtElement.COMPOUND_TYPE);
			for (NbtElement elem : bounties) {
				NbtCompound entry = (NbtCompound)elem;
				tooltip.add(
					new TranslatableText(Registry.ITEM.get(new Identifier(entry.getString("Name"))).getTranslationKey()).formatted(Formatting.GRAY)
					.append(" ")
					.append(String.valueOf(entry.getInt("Count")))
					.append(" / ")
					.append(String.valueOf(entry.getInt("Needed")))
				);
			}
		}
		if (tag.contains("Entities")) {
			NbtList bounties = tag.getList("Entities", NbtElement.COMPOUND_TYPE);
			for (NbtElement elem : bounties) {
				NbtCompound entry = (NbtCompound)elem;
				tooltip.add(
					new TranslatableText(Registry.ENTITY_TYPE.get(new Identifier(entry.getString("Name"))).getTranslationKey()).formatted(Formatting.GRAY)
					.append(" ")
					.append(String.valueOf(entry.getInt("Count")))
					.append(" / ")
					.append(String.valueOf(entry.getInt("Needed")))
				);
			}
		}
		if (tag.contains("Rewards")) {
			tooltip.add(new TranslatableText("item.guild.quest_scroll.rewards").formatted(Formatting.GREEN));
			NbtList bounties = tag.getList("Rewards", NbtElement.COMPOUND_TYPE);
			for (NbtElement elem : bounties) {
				NbtCompound entry = (NbtCompound)elem;
				tooltip.add(
					new TranslatableText(Registry.ITEM.get(new Identifier(entry.getString("Name"))).getTranslationKey()).formatted(Formatting.GRAY)
					.append(" ")
					.append(String.valueOf(entry.getInt("Count")))
				);
			}
		}
	}
}
