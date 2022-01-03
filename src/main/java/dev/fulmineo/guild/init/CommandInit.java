package dev.fulmineo.guild.init;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.GuildServerPlayerEntity;
import dev.fulmineo.guild.data.Quest;
import dev.fulmineo.guild.data.QuestHelper;
import dev.fulmineo.guild.data.QuestProfession;
import dev.fulmineo.guild.data.ServerDataManager;

public class CommandInit {
	public static ProfessionsSuggestionProvider PROFESSION_SUGGESTION_PROVIDER = new ProfessionsSuggestionProvider();
	public static void init(){
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(
				CommandManager.literal("guild")
				.then(
					CommandManager.literal("reset")
					.executes(context -> {
						ServerCommandSource source = context.getSource();
						if (source != null) {
							ServerPlayerEntity player = source.getPlayer();
							((GuildServerPlayerEntity)player).resetQuestsAndProfessions();
							context.getSource().sendFeedback(new TranslatableText("command.guild.reset.success"), false);
						}
						return 1;
					})
				)
			);
            dispatcher.register(
				CommandManager.literal("guild")
				.then(
					CommandManager.literal("clear")
					.executes(context -> {
						ServerCommandSource source = context.getSource();
						if (source != null) {
							ServerPlayerEntity player = source.getPlayer();
							((GuildServerPlayerEntity)player).clearQuests();
							context.getSource().sendFeedback(new TranslatableText("command.guild.clear.success"), false);
						}
						return 1;
					})
				)
			);
			dispatcher.register(
				CommandManager.literal("guild")
				.then(
					CommandManager.literal("licence")
					.requires(source -> source.hasPermissionLevel(2))
					.then(
						CommandManager.argument("profession", StringArgumentType.string())
						.suggests(PROFESSION_SUGGESTION_PROVIDER)
						.executes(context -> {
							ServerCommandSource source = context.getSource();
							if (source != null) {
								ServerPlayerEntity player = source.getPlayer();
								String professionName = StringArgumentType.getString(context, "profession");
								QuestProfession profession = ServerDataManager.professions.get(professionName);
								if (profession == null) {
									source.sendFeedback(new TranslatableText("command.guild.licence.invalid_profession", professionName), false);
								} else {
									ItemStack stack = new ItemStack(Guild.QUEST_PROFESSION_LICENCE_ITEM);
									NbtCompound nbt = stack.getOrCreateNbt();
									nbt.putString("Profession", professionName);
									stack.setNbt(nbt);
									if (!player.giveItemStack(stack)) {
										player.dropItem(stack, false);
									}
								}
							}
							return 1;
						})
					)
				)
			);
			dispatcher.register(
				CommandManager.literal("guild")
				.then(
					CommandManager.literal("quest")
					.requires(source -> source.hasPermissionLevel(2))
					.then(
						CommandManager.argument("profession", StringArgumentType.string())
						.suggests(PROFESSION_SUGGESTION_PROVIDER)
						.executes(context -> {
							ServerCommandSource source = context.getSource();
							if (source != null) {
								ServerPlayerEntity player = source.getPlayer();
								String professionName = StringArgumentType.getString(context, "profession");
								QuestProfession profession = ServerDataManager.professions.get(professionName);
								if (profession == null) {
									source.sendFeedback(new TranslatableText("command.guild.licence.invalid_profession", professionName), false);
								} else {
									List<Quest> professionQuests = ((GuildServerPlayerEntity)player).getAvailableQuests().get(professionName);
									if (professionQuests == null || professionQuests.size() < Guild.CONFIG.getMaxQuestsPerProfession()) {
										List<QuestProfession> availableProfessions = new ArrayList<>();
										availableProfessions.add(profession);
										QuestHelper.generateQuests(player, availableProfessions, 1);
									}
								}
							}
							return 1;
						})
					)
				)
			);
			dispatcher.register(
				CommandManager.literal("guild")
				.then(
					CommandManager.literal("exp")
					.requires(source -> source.hasPermissionLevel(2))
					.then(
						CommandManager.argument("profession", StringArgumentType.string())
						.suggests(PROFESSION_SUGGESTION_PROVIDER)
						.then(
							CommandManager.argument("exp", IntegerArgumentType.integer())
							.executes(context -> {
								ServerCommandSource source = context.getSource();
								if (source != null) {
									ServerPlayerEntity player = source.getPlayer();
									String professionName = StringArgumentType.getString(context, "profession");
									QuestProfession profession = ServerDataManager.professions.get(professionName);
									if (profession == null) {
										source.sendFeedback(new TranslatableText("command.guild.licence.invalid_profession", professionName), false);
									} else {
										int exp = IntegerArgumentType.getInteger(context, "exp");
										((GuildServerPlayerEntity)player).setProfessionExp(professionName, exp);
									}
								}
								return 1;
							})
						)
					)
				)
			);
		});
	}

	public static class ProfessionsSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
		@Override
		public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
			for (String professionName: ServerDataManager.professions.keySet()) {
				builder.suggest('"'+professionName+'"');
			}
			return builder.buildFuture();
		}
	}
}
