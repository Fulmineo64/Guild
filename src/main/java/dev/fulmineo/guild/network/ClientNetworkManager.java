package dev.fulmineo.guild.network;

import java.util.List;
import java.util.Map;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.ClientDataManager;
import dev.fulmineo.guild.data.Quest;
import dev.fulmineo.guild.data.QuestProfessionRequirement;
import dev.fulmineo.guild.data.Range;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;

public class ClientNetworkManager {
	public static void registerServerReceiver() {
		ClientPlayNetworking.registerGlobalReceiver(Guild.TRANSFER_CLIENT_DATA_ID, (client, handler, buf, responseSender) -> {
			ClientDataManager.professionsLabels.clear();
			ClientDataManager.professionRequirements.clear();
			NbtCompound nbt = buf.readNbt();
			NbtCompound professionsLabels = nbt.getCompound("Labels");
			for (String professionName: professionsLabels.getKeys()) {
				ClientDataManager.professionsLabels.put(professionName, professionsLabels.getString(professionName));
			}
			NbtCompound professionsRequirements = nbt.getCompound("Requirements");
			for (String professionName: professionsRequirements.getKeys()) {
				NbtList requirementsList = professionsRequirements.getList(professionName, NbtElement.COMPOUND_TYPE);
				int length = requirementsList.size();
				QuestProfessionRequirement[] reqs = new QuestProfessionRequirement[length];
				for (int i = 0; i < length; i++) {
					NbtCompound entry = (NbtCompound)requirementsList.get(i);
					QuestProfessionRequirement req = new QuestProfessionRequirement();
					if (entry.contains("Profession")) {
						req.profession = entry.getString("Profession");
					}
					if (entry.contains("Level")) {
						NbtCompound level = entry.getCompound("Level");
						req.level = new Range();
						if (level.contains("Min")) {
							req.level.min = level.getInt("Min");
						}
						if (level.contains("Max")) {
							req.level.max = level.getInt("Max");
						}
					}
					reqs[i] = req;
				}
				ClientDataManager.professionRequirements.put(professionName, reqs);
			}
		});
	}

	public static void openGuildScreen(){
		ClientPlayNetworking.send(Guild.OPEN_QUESTS_SCREEN_PACKET_ID, PacketByteBufs.empty());
	}

	public static void acceptQuest(String profession, int index) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeString(profession);
		buf.writeInt(index);
		ClientPlayNetworking.send(Guild.ACCEPT_QUEST_PACKET_ID, buf);
	}

	public static void deleteAcceptedQuest(List<Quest> acceptedQuests, int index) {
		if (acceptedQuests.size() > index) {
			acceptedQuests.remove(index);
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt(index);
			ClientPlayNetworking.send(Guild.DELETE_ACCEPTED_QUEST_PACKET_ID, buf);
		}
	}

	public static void deleteAvailableQuest(Map<String, List<Quest>> availableQuest, String profession, int index) {
		List<Quest> quests = availableQuest.get(profession);
		if (quests.size() > index) {
			quests.remove(index);
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeString(profession);
			buf.writeInt(index);
			ClientPlayNetworking.send(Guild.DELETE_AVAILABLE_QUEST_PACKET_ID, buf);
		}
	}

	public static void requestClientData() {
		ClientPlayNetworking.send(Guild.REQUEST_CLIENT_DATA_ID, PacketByteBufs.create());
	}
}
