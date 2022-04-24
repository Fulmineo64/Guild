package dev.fulmineo.guild.init;

import java.util.List;

import com.epherical.octoecon.api.event.EconomyEvents;
import dev.fulmineo.guild.EconomyDependency;
import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.network.ServerNetworkManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerEventInit {
	public static void init() {
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(new ServerLifecycleEvents.EndDataPackReload(){
			public void endDataPackReload(MinecraftServer server, LifecycledResourceManager serverResourceManager, boolean success) {
				NbtCompound nbt = ServerNetworkManager.getClientDataNbt();
				List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
				for (ServerPlayerEntity player: playerList) {
					ServerNetworkManager.sendDataToClient(player, nbt);
				}
			}
		});
		if (Guild.OCTO_ECON_API_LOADED) {
			EconomyEvents.ECONOMY_CHANGE_EVENT.register(economy -> {
				Guild.economyDependency = new EconomyDependency(economy);
			});
		}
	}
}
