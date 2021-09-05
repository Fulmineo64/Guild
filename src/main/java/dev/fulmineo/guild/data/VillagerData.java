package dev.fulmineo.guild.data;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;

import dev.fulmineo.guild.Guild;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public class VillagerData {
	// Points of Interest
	public static final PointOfInterestType GUILD_MASTER_POI = PointOfInterestType.register("guild_master", PointOfInterestType.getAllStatesOf(Guild.GUILD_MASTER_TABLE), 1, 1);

	// Villager Professions
	public static final VillagerProfession GUILD_MASTER = VillagerProfession.register("guild_master", GUILD_MASTER_POI, SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER);

	public static void refreshTrades(Map<String, QuestProfession> professions) {
		// GUILD_MASTER trades
		Map<Integer, List<TradeOffers.Factory>> offersByLevel = new HashMap<>();
		for (Entry<String, QuestProfession> entry: professions.entrySet()) {
			String professionName = entry.getKey();
			QuestProfession prof = entry.getValue();
			int level = prof.guildMasterLevel == 0 ? 1 : (prof.guildMasterLevel > 5 ? 5 : prof.guildMasterLevel);
			List<TradeOffers.Factory> offers = offersByLevel.get(level);
			if (offers == null) {
				offers = new ArrayList<>();
			}
			offers.add(new SellGuildProfessionItemFactory(Guild.QUEST_PROFESSION_LICENCE_ITEM, professionName, 5 * level, 5 * level));
			offersByLevel.put(level, offers);
		}
		ImmutableMap.Builder<Integer, TradeOffers.Factory[]> builder = ImmutableMap.builder();
		for (Entry<Integer, List<TradeOffers.Factory>> entry: offersByLevel.entrySet()) {
			TradeOffers.Factory[] array = new TradeOffers.Factory[entry.getValue().size()];
			entry.getValue().toArray(array);
			builder.put(entry.getKey(), array);
		}
		TradeOffers.PROFESSION_TO_LEVELED_TRADE.put(GUILD_MASTER, VillagerData.toIntMap(builder.build()));
	}

	private static Int2ObjectMap<TradeOffers.Factory[]> toIntMap(ImmutableMap<Integer, TradeOffers.Factory[]> trades) {
        return new Int2ObjectOpenHashMap<>(trades);
    }

	public static class SellGuildProfessionItemFactory implements TradeOffers.Factory {
		private final Item sell;
		private final String profession;
		private final int price;
		private final int experience;

		public SellGuildProfessionItemFactory(ItemConvertible item, String profession, int price, int experience) {
		   this.sell = item.asItem();
		   this.profession = profession;
		   this.price = price;
		   this.experience = experience;
		}

		public TradeOffer create(Entity entity, Random random) {
			ItemStack stack = new ItemStack(this.sell);
			NbtCompound nbt = stack.getOrCreateNbt();
			nbt.putString("Profession", this.profession);
		   	return new TradeOffer(new ItemStack(Items.EMERALD, this.price), stack, 99999, this.experience, 0.05F);
		}
	 }

}
