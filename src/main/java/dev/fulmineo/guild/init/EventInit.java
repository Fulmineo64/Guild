package dev.fulmineo.guild.init;

import java.util.List;

import dev.fulmineo.guild.network.ClientNetworkManager;
import dev.fulmineo.guild.network.ServerNetworkManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class EventInit {
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register(new ClientPlayConnectionEvents.Join(){
			public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
				ClientNetworkManager.requestClientData();
			}
		});

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(new ServerLifecycleEvents.EndDataPackReload(){
			public void endDataPackReload(MinecraftServer server, ServerResourceManager serverResourceManager, boolean success) {
				NbtCompound nbt = ServerNetworkManager.getClientDataNbt();
				List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
				for (ServerPlayerEntity player: playerList) {
					ServerNetworkManager.sendDataToClient(player, nbt);
				}
			}
		});
	}
}
