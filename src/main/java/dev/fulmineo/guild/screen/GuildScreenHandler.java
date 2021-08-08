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

public class GuildScreenHandler extends ScreenHandler {
	// private final Inventory inventory;

	public Map<String, List<Quest>> availableQuests;
	public List<Quest> acceptedQuests;
	public List<QuestProfession> professions;
	public Map<String, Integer> professionsExp;

	public GuildScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf){
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
			profession.levels = professionInfo.getString("Levels");
			this.professionsExp.put(profession.name, professionInfo.getInt("Exp"));
			this.professions.add(profession);
		}
    }

    public GuildScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(Guild.GUILD_SCREEN_HANDLER, syncId);
	}

	public boolean canUse(PlayerEntity player) {
		// TODO: Check if the player has at least 1 guild
        return true;
    }

	@Environment(EnvType.CLIENT)
	public void acceptQuest(String profession, int index) {
		List<Quest> professionsQuest = this.availableQuests.get(profession);
		Quest quest = professionsQuest.remove(index);
		this.acceptedQuests.add(quest);
		ClientNetworkManager.acceptQuest(profession, index);
	}

	@Environment(EnvType.CLIENT)
	public void tryCompleteQuest(int index) {
		ClientNetworkManager.tryCompleteQuest(this.acceptedQuests, this.professionsExp, index);
	}
}
