package dev.fulmineo.guild.network;

import dev.fulmineo.guild.Guild;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class ClientNetworkManager {
	public static void openGuildScreen(){
		ClientPlayNetworking.send(Guild.OPEN_GUILD_SCREEN_PACKET_ID, PacketByteBufs.empty());
	}
}
