package dev.fulmineo.guild.network;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.QuestProfession;
import dev.fulmineo.guild.data.QuestProfessionRequirement;
import dev.fulmineo.guild.screen.QuestsScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import dev.fulmineo.guild.data.ServerDataManager;
import dev.fulmineo.guild.data.GuildServerPlayerEntity;
import dev.fulmineo.guild.data.Quest;
import dev.fulmineo.guild.data.QuestHelper;
import dev.fulmineo.guild.data.QuestLevel;
import dev.fulmineo.guild.data.QuestPoolData;

public class ServerNetworkManager {
	public static void registerClientReceiver() {
		ServerPlayNetworking.registerGlobalReceiver(Guild.OPEN_QUESTS_SCREEN_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			server.execute(new Runnable() {
				public void run(){
					if (Guild.errors.size() > 0) {
						player.sendMessage(Text.translatable("lang.guild.errors"), false);
						for (String error: Guild.errors) {
							player.sendMessage(Text.literal(" - ").append(error), false);
						}
						player.sendMessage(Text.translatable("lang.guild.errors_info"), false);
						return;
					}

					GuildServerPlayerEntity guildPlayer = ((GuildServerPlayerEntity)player);
					if (guildPlayer.getQuestProfessions().size() == 0) {
						player.sendMessage(Text.translatable("profession.missing"), false);
						return;
					}

					player.openHandledScreen(new ExtendedScreenHandlerFactory() {
						@Override
						public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
							List<QuestProfession> professions = guildPlayer.getQuestProfessions();

							// Quests get generated at a fixed interval, so here we check the number of quests to generate based on that interval
							QuestHelper.refreshAvailableQuests(professions, player);

							NbtCompound nbt = QuestHelper.writeMapNbt(new NbtCompound(), guildPlayer.getAvailableQuests());
							nbt = QuestHelper.writeNbt(nbt, guildPlayer.getAcceptedQuests());
							NbtList professionsInfo = new NbtList();
							for (QuestProfession profession: professions) {
								NbtCompound tag = new NbtCompound();
								tag.putString("Name", profession.name);
								tag.putString("Icon", profession.icon);
								professionsInfo.add(QuestHelper.writeProfessionData(tag, guildPlayer, profession));
							}
							nbt.put("Professions", professionsInfo);
							nbt.putInt("MaxAcceptedQuests", Guild.CONFIG.getMaxAcceptedQuests());
							packetByteBuf.writeNbt(nbt);
						}

						@Override
						public Text getDisplayName() {
							return Text.translatable("screen.guild.quests");
						}

						@Override
						public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
							return new QuestsScreenHandler(syncId, inv);
						}
					});
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(Guild.ACCEPT_QUEST_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			GuildServerPlayerEntity guildPlayer = (GuildServerPlayerEntity)player;
			List<Quest> acceptedQuests = guildPlayer.getAcceptedQuests();
			if (acceptedQuests.size() < Guild.CONFIG.getMaxAcceptedQuests()) {
				String profession = buf.readString();
				int index = buf.readInt();
				Map<String, List<Quest>> availableQuests = guildPlayer.getAvailableQuests();
				List<Quest> quests = availableQuests.get(profession);
				Quest quest = quests.remove(index);
				quest.accept(player.world.getTime());
				acceptedQuests.add(quest);
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(Guild.TRY_COMPLETE_QUEST_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			int index = buf.readInt();
			GuildServerPlayerEntity guildPlayer = (GuildServerPlayerEntity)player;
			List<Quest> acceptedQuests = guildPlayer.getAcceptedQuests();
			Quest quest = acceptedQuests.get(index);
			int level = 0;
			String professionName = quest.getProfessionName();
			QuestProfession profession = ServerDataManager.professions.get(professionName);
			List<QuestLevel> levels = ServerDataManager.levels.get(profession.levelsPool);
			if (profession != null) {
				level = QuestHelper.getCurrentLevel(levels, guildPlayer.getProfessionExp(professionName));
			}
			boolean ok = quest.tryComplete(player);
			if (profession != null) {
				int newLevel = QuestHelper.getCurrentLevel(levels, guildPlayer.getProfessionExp(professionName));
				if (level != newLevel) {
					newLevel++;
					player.sendMessage(Text.translatable("profession.level_up", newLevel, QuestProfession.getTranslatedText(professionName)), false);
					if (Guild.CONFIG.displayUnlockedPools) {
						boolean sentDescription = false;
						for (QuestPoolData task: profession.tasks) {
							if (task.level != null && task.level.min == newLevel) {
								if (!sentDescription) {
									sentDescription = true;
									player.sendMessage(Text.translatable("profession.unlocked_tasks"), false);
								}
								String icon = "";
								String translationKey = "";
								switch (task.type) {
									case "item": {
										icon = "✉";
										translationKey = Registry.ITEM.get(new Identifier(task.name)).getTranslationKey();
										break;
									}
									case "slay": {
										icon = "🗡";
										translationKey = Registry.ENTITY_TYPE.get(new Identifier(task.name)).getTranslationKey();
										break;
									}
									case "cure": {
										icon = "✙";
										translationKey = Registry.ENTITY_TYPE.get(new Identifier(task.name)).getTranslationKey();
										break;
									}
									case "summon": {
										icon = "✦";
										translationKey = Registry.ENTITY_TYPE.get(new Identifier(task.name)).getTranslationKey();
										break;
									}
								}
								player.sendMessage(Text.literal(" "+icon+" ").append(Text.translatable(translationKey)), false);
							}
						}
						sentDescription = false;
						for (QuestPoolData reward: profession.rewards) {
							if (reward.level != null && reward.level.min == newLevel) {
								if (!sentDescription) {
									sentDescription = true;
									player.sendMessage(Text.translatable("profession.unlocked_rewards"), false);
								}
								player.sendMessage((Text.translatable(Registry.ITEM.get(new Identifier(reward.name)).getTranslationKey())), false);
							}
						}
					}
				}
			}
			PacketByteBuf buffer = PacketByteBufs.create();
			buffer.writeBoolean(ok);
			if (ok) {
				acceptedQuests.remove(index);
				if (profession != null) {
					buffer.writeNbt(QuestHelper.writeProfessionData(new NbtCompound(), guildPlayer, profession));
				}
			}
			responseSender.sendPacket(Guild.TRY_COMPLETE_QUEST_PACKET_ID, buffer);
		});

		ServerPlayNetworking.registerGlobalReceiver(Guild.DELETE_ACCEPTED_QUEST_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			((GuildServerPlayerEntity)player).getAcceptedQuests().remove(buf.readInt());
		});

		ServerPlayNetworking.registerGlobalReceiver(Guild.DELETE_AVAILABLE_QUEST_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			((GuildServerPlayerEntity)player).getAvailableQuests().get(buf.readString()).remove(buf.readInt());
		});

		ServerPlayNetworking.registerGlobalReceiver(Guild.REQUEST_CLIENT_DATA_ID, (server, player, handler, buf, responseSender) -> {
			ServerNetworkManager.sendDataToClient(player, ServerNetworkManager.getClientDataNbt());
		});
	}

	public static NbtCompound getClientDataNbt() {
		NbtCompound nbt = new NbtCompound();
		NbtCompound professionLabels = new NbtCompound();
		NbtCompound professionRequirements = new NbtCompound();
		for (QuestProfession profession: ServerDataManager.professions.values()) {
			if (profession.label != null) {
				professionLabels.put(profession.name, NbtString.of(profession.label));
			}
			if (profession.requirements != null) {
				NbtList requirements = new NbtList();
				for (QuestProfessionRequirement req: profession.requirements) {
					NbtCompound requirement = new NbtCompound();
					if (req.profession != null) {
						requirement.putString("Profession", req.profession);
					}
					if (req.level != null) {
						NbtCompound level = new NbtCompound();
						if (req.level.min != null) {
							level.putInt("Min", req.level.min);
						}
						if (req.level.max != null) {
							level.putInt("Max", req.level.max);
						}
						requirement.put("Level", level);
					}
					requirements.add(requirement);
				}
				professionRequirements.put(profession.name, requirements);
			}
		}
		nbt.put("Labels", professionLabels);
		nbt.put("Requirements", professionRequirements);
		return nbt;
	}

	public static void sendDataToClient(ServerPlayerEntity player, NbtCompound nbt) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeNbt(nbt);
		ServerPlayNetworking.send(player, Guild.TRANSFER_CLIENT_DATA_ID, buf);
	}
}
