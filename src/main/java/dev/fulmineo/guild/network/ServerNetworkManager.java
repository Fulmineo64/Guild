package dev.fulmineo.guild.network;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.QuestProfession;
import dev.fulmineo.guild.screen.GuildScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.screen.ScreenHandler;
import dev.fulmineo.guild.data.GuildServerPlayerEntity;
import dev.fulmineo.guild.data.QuestHelper;

public class ServerNetworkManager {
	public static void registerClientReceiver() {
		ServerPlayNetworking.registerGlobalReceiver(Guild.OPEN_GUILD_SCREEN_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			server.execute(new Runnable() {
				public void run(){
					player.openHandledScreen(new ExtendedScreenHandlerFactory() {
						@Override
						public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
							// packetByteBuf.writeEnumConstant(hand);
							List<QuestProfession> professions = ((GuildServerPlayerEntity)serverPlayerEntity).getQuestProfessions();

							// Quests get generated at a fixed interval, so here we check the number of quests to generate based on that interval
							QuestHelper.refreshAvailableQuests(professions, serverPlayerEntity);

							// inventory.onOpen(playerInventory.player);
							packetByteBuf.writeNbt(QuestHelper.writeMapNbt(new NbtCompound(), ((GuildServerPlayerEntity)serverPlayerEntity).getAvailableQuests()));
						}

						@Override
						public Text getDisplayName() {
							return new TranslatableText("lang.guild.guild_screen");
						}

						@Override
						public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
							return new GuildScreenHandler(syncId, inv, ((GuildServerPlayerEntity)player).getAvailableQuests());
						}
					});
				}
			});
		});
	}
}
