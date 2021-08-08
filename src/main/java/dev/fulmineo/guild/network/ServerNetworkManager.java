package dev.fulmineo.guild.network;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.QuestProfession;
import dev.fulmineo.guild.screen.GuildScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.screen.ScreenHandler;
import dev.fulmineo.guild.data.GuildServerPlayerEntity;
import dev.fulmineo.guild.data.Quest;
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
							GuildServerPlayerEntity guildPlayer = ((GuildServerPlayerEntity)player);
							NbtCompound nbt = QuestHelper.writeMapNbt(new NbtCompound(), guildPlayer.getAvailableQuests());
							nbt = QuestHelper.writeNbt(nbt, guildPlayer.getAcceptedQuests());
							NbtList professionsInfo = new NbtList();
							for (QuestProfession profession: professions) {
								NbtCompound tag = new NbtCompound();
								tag.putString("Name", profession.name);
								tag.putString("Icon", profession.icon);
								professionsInfo.add(tag);
							}
							nbt.put("Professions", professionsInfo);
							packetByteBuf.writeNbt(nbt);
						}

						@Override
						public Text getDisplayName() {
							return new TranslatableText("lang.guild.guild_screen");
						}

						@Override
						public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
							return new GuildScreenHandler(syncId, inv);
						}
					});
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(Guild.ACCEPT_QUEST_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			String profession = buf.readString();
			int index = buf.readInt();
			GuildServerPlayerEntity guildPlayer = (GuildServerPlayerEntity)player;
			Map<String, List<Quest>> availableQuests = guildPlayer.getAvailableQuests();
			List<Quest> quests = availableQuests.get(profession);
			Quest quest = quests.remove(index);
			List<Quest> acceptedQuests = guildPlayer.getAcceptedQuests();
			acceptedQuests.add(quest);
		});

		ServerPlayNetworking.registerGlobalReceiver(Guild.TRY_COMPLETE_QUEST_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			int index = buf.readInt();
			List<Quest> acceptedQuests = ((GuildServerPlayerEntity)player).getAcceptedQuests();
			// TODO: Here check if the player has all the requirements
			Quest quest = acceptedQuests.get(index);
			boolean ok = quest.tryComplete(player);
			if (ok) acceptedQuests.remove(index);
			PacketByteBuf buffer = PacketByteBufs.create();
			buffer.writeBoolean(ok);
			responseSender.sendPacket(Guild.TRY_COMPLETE_QUEST_PACKET_ID, buffer);
		});
	}
}
