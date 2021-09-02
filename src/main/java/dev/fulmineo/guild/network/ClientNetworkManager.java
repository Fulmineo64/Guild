package dev.fulmineo.guild.network;

import java.util.List;
import java.util.Map;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.Quest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class ClientNetworkManager {
	public static void openGuildScreen(){
		ClientPlayNetworking.send(Guild.OPEN_QUESTS_SCREEN_PACKET_ID, PacketByteBufs.empty());
	}

	public static void acceptQuest(String profession, int index) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeString(profession);
		buf.writeInt(index);
		ClientPlayNetworking.send(Guild.ACCEPT_QUEST_PACKET_ID, buf);
	}

	public static void tryCompleteQuest(List<Quest> acceptedQuests, Map<String, Integer> professionsExp, int index) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(index);
		ClientPlayNetworking.registerReceiver(Guild.TRY_COMPLETE_QUEST_PACKET_ID, (client, player, buffer, responseSender) -> {
			if (buffer.readBoolean()) {
				Quest quest = acceptedQuests.remove(index);
				professionsExp.put(quest.getProfessionName(), buffer.readInt());
			}
			ClientPlayNetworking.unregisterReceiver(Guild.TRY_COMPLETE_QUEST_PACKET_ID);
		});
		ClientPlayNetworking.send(Guild.TRY_COMPLETE_QUEST_PACKET_ID, buf);
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
}
