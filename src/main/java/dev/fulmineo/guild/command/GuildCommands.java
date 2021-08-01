package dev.fulmineo.guild.command;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import dev.fulmineo.guild.data.DataManager;
import dev.fulmineo.guild.data.Quest;
import dev.fulmineo.guild.data.GuildServerPlayerEntity;

public class GuildCommands {
	public static void init(){
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(
				CommandManager.literal("quest")
				.requires(source -> source.hasPermissionLevel(2))
				.executes(context -> {
					ServerCommandSource source = context.getSource();
					if (source != null) {
						ServerPlayerEntity player = source.getPlayer();
						((GuildServerPlayerEntity)player).acceptQuest(Quest.create(DataManager.professions.get("guild:guard"), player.world.getTime()));
					}
					return 1;
				})
			);
		});
	}
}
