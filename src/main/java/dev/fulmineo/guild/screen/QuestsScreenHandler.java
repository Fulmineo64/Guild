package dev.fulmineo.guild.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.Quest;
import dev.fulmineo.guild.data.QuestHelper;
import dev.fulmineo.guild.data.QuestProfession;
import dev.fulmineo.guild.network.ClientNetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;

public class QuestsScreenHandler extends ScreenHandler {
	// private final Inventory inventory;

	public Map<String, List<Quest>> availableQuests;
	public List<Quest> acceptedQuests;
	public List<QuestProfession> professions;
	public Map<String, Integer> professionsExp;
	public World world;

	public QuestsScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf){
        this(syncId, playerInventory);
		NbtCompound nbt = buf.readNbt();
		this.availableQuests = QuestHelper.fromMapNbt(nbt);
		this.acceptedQuests = QuestHelper.fromNbt(nbt);
		this.professions = new ArrayList<>();
		this.professionsExp = new HashMap<>();
		NbtList professionsInfo = nbt.getList("Professions", NbtElement.COMPOUND_TYPE);
		for (NbtElement entry: professionsInfo) {
			NbtCompound professionInfo = (NbtCompound)entry;
			QuestProfession profession = new QuestProfession();
			profession.name = professionInfo.getString("Name");
			profession.icon = professionInfo.getString("Icon");
			profession.levelsPool = professionInfo.getString("Levels");
			this.professionsExp.put(profession.name, professionInfo.getInt("Exp"));
			this.professions.add(profession);
		}
		this.world = playerInventory.player.world;
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
	}

	@Environment(EnvType.CLIENT)
	public void tryCompleteQuest(int index) {
		ClientNetworkManager.tryCompleteQuest(this.acceptedQuests, this.professionsExp, index);
	}

	@Environment(EnvType.CLIENT)
	public void deleteAcceptedQuest(int index) {
		ClientNetworkManager.deleteAcceptedQuest(this.acceptedQuests, index);
	}

	@Environment(EnvType.CLIENT)
	public void deleteAvailableQuest(String profession, int index) {
		ClientNetworkManager.deleteAvailableQuest(this.availableQuests, profession, index);
	}
}
