package dev.fulmineo.guild.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.Quest;
import dev.fulmineo.guild.data.QuestHelper;
import dev.fulmineo.guild.data.QuestProfession;
import dev.fulmineo.guild.network.ClientNetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class QuestsScreenHandler extends ScreenHandler {
	// private final Inventory inventory;

	public Map<String, List<Quest>> availableQuests;
	public List<Quest> acceptedQuests;
	public List<QuestProfession> professions;
	public Map<String, ProfessionData> professionsData;
	public World world;
	public Map<String, Integer> itemCount = new HashMap<>();

	public QuestsScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf){
        this(syncId, playerInventory);
		NbtCompound nbt = buf.readNbt();
		this.availableQuests = QuestHelper.fromMapNbt(nbt);
		this.acceptedQuests = QuestHelper.fromNbt(nbt);
		this.professions = new ArrayList<>();
		this.professionsData = new HashMap<>();
		NbtList professionsInfo = nbt.getList("Professions", NbtElement.COMPOUND_TYPE);
		for (NbtElement entry: professionsInfo) {
			NbtCompound professionInfo = (NbtCompound)entry;
			QuestProfession profession = new QuestProfession();
			profession.name = professionInfo.getString("Name");
			profession.icon = professionInfo.getString("Icon");
			ProfessionData data = new ProfessionData();
			data.exp = professionInfo.getInt("Exp");
			data.level = professionInfo.getInt("Level");
			data.levelPerc = professionInfo.getInt("LevelPerc");
			data.levelMax = professionInfo.getBoolean("LevelMax");
			this.professions.add(profession);
			this.professionsData.put(profession.name, data);
		}
		this.world = playerInventory.player.world;
		// Count inventory items
		ImmutableList<DefaultedList<ItemStack>> mainAndOffhand = ImmutableList.of(playerInventory.main, playerInventory.offHand);
		Iterator<DefaultedList<ItemStack>> iterator = mainAndOffhand.iterator();
		while (iterator.hasNext()) {
			DefaultedList<ItemStack> defaultedList = (DefaultedList<ItemStack>) iterator.next();
			for (int i = 0; i < defaultedList.size(); ++i) {
				ItemStack stack = defaultedList.get(i);
				if (!stack.isOf(Items.AIR)) {
					String id = Registry.ITEM.getId(stack.getItem()).toString();
					Integer val = this.itemCount.get(id);
					this.itemCount.put(id, (val == null ? 0 : val) + stack.getCount());
				}
			}
		}
		this.updateItemCompletion();
    }

    public QuestsScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(Guild.QUESTS_SCREEN_HANDLER, syncId);
	}

	public boolean canUse(PlayerEntity player) {
		return true;
    }

	@Environment(EnvType.CLIENT)
	public void acceptQuest(String profession, int index) {
		List<Quest> professionsQuest = this.availableQuests.get(profession);
		if (professionsQuest == null || professionsQuest.size() <= index) return;
		Quest quest = professionsQuest.remove(index);
		quest.accept(this.world.getTime());
		this.acceptedQuests.add(quest);
		ClientNetworkManager.acceptQuest(profession, index);
		this.updateItemCompletion();
	}

	@Environment(EnvType.CLIENT)
	public void tryCompleteQuest(int index) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(index);
		ClientPlayNetworking.registerReceiver(Guild.TRY_COMPLETE_QUEST_PACKET_ID, (client, player, buffer, responseSender) -> {
			if (buffer.readBoolean()) {
				Quest quest = this.acceptedQuests.remove(index);
				NbtCompound nbt = buffer.readNbt();
				ProfessionData data = this.professionsData.get(quest.getProfessionName());
				data.exp = nbt.getInt("Exp");
				data.level = nbt.getInt("Level");
				data.levelPerc = nbt.getInt("LevelPerc");
				data.levelMax = nbt.getBoolean("LevelMax");
				for (NbtElement elm: quest.getItemList()) {
					NbtCompound entry = (NbtCompound)elm;
					String item = entry.getString("Name");
					this.itemCount.put(item, this.itemCount.get(item) - entry.getInt("Count"));
				}
				this.updateItemCompletion();
			}
			ClientPlayNetworking.unregisterReceiver(Guild.TRY_COMPLETE_QUEST_PACKET_ID);
		});
		ClientPlayNetworking.send(Guild.TRY_COMPLETE_QUEST_PACKET_ID, buf);
	}

	@Environment(EnvType.CLIENT)
	public void deleteAcceptedQuest(int index) {
		ClientNetworkManager.deleteAcceptedQuest(this.acceptedQuests, index);
	}

	@Environment(EnvType.CLIENT)
	public void deleteAvailableQuest(String profession, int index) {
		ClientNetworkManager.deleteAvailableQuest(this.availableQuests, profession, index);
	}

	private void updateItemCompletion() {
		for (Quest quest: this.acceptedQuests) {
			NbtList items = quest.getItemList();
			for (NbtElement elm: items) {
				NbtCompound entry = (NbtCompound)elm;
				Integer val = this.itemCount.get(entry.getString("Name"));
				entry.putInt("Count", Math.min(val == null ? 0 : val, entry.getInt("Needed")));
			}
		}
	}

	public class ProfessionData {
		public int exp;
		public int level;
		public int levelPerc;
		public boolean levelMax;
	}
}
