package dev.fulmineo.guild.command;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import dev.fulmineo.guild.data.GuildServerPlayerEntity;

public class GuildCommands {
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
		});
	}
}
