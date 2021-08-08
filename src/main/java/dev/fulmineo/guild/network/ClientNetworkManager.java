package dev.fulmineo.guild.network;

import java.util.List;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.Quest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class ClientNetworkManager {
	public static void openGuildScreen(){
		ClientPlayNetworking.send(Guild.OPEN_GUILD_SCREEN_PACKET_ID, PacketByteBufs.empty());
	}

	public static void acceptQuest(String profession, int index) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeString(profession);
		buf.writeInt(index);
		ClientPlayNetworking.send(Guild.ACCEPT_QUEST_PACKET_ID, buf);
	}

	public static void tryCompleteQuest(List<Quest> acceptedQuests, int index) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(index);
		ClientPlayNetworking.registerReceiver(Guild.TRY_COMPLETE_QUEST_PACKET_ID, (client, player, buffer, responseSender) -> {
			if (buffer.readBoolean()) {
				Guild.info("ciao");
				acceptedQuests.remove(index);
			}
			ClientPlayNetworking.unregisterReceiver(Guild.TRY_COMPLETE_QUEST_PACKET_ID);
		});
		ClientPlayNetworking.send(Guild.TRY_COMPLETE_QUEST_PACKET_ID, buf);
	}
}
