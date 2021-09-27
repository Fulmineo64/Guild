package dev.fulmineo.guild.init;

import dev.fulmineo.guild.network.ClientNetworkManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public class ClientEventInit {
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register(new ClientPlayConnectionEvents.Join(){
			public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
				ClientNetworkManager.requestClientData();
			}
		});
	}
}
