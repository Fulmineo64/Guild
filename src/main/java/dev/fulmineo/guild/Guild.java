package dev.fulmineo.guild;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.fulmineo.guild.command.GuildCommands;
import dev.fulmineo.guild.data.DataManager;

import dev.fulmineo.guild.network.ServerNetworkManager;
import dev.fulmineo.guild.screen.GuildScreenHandler;

public class Guild implements ModInitializer {

	public static Logger LOGGER = LogManager.getLogger();

    // Identifiers

    public static final String MOD_ID = "guild";
	public static final Identifier OPEN_GUILD_SCREEN_PACKET_ID = new Identifier(MOD_ID, "guild_screen_packet");
    public static final Identifier ACCEPT_QUEST_PACKET_ID = new Identifier(MOD_ID, "accept_quest_packet");
    public static final Identifier TRY_COMPLETE_QUEST_PACKET_ID = new Identifier(MOD_ID, "complete_quest_packet");
    public static final ScreenHandlerType<GuildScreenHandler> GUILD_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID, "guild_screen"), GuildScreenHandler::new);

	// Items

	public static final ItemGroup GROUP = FabricItemGroupBuilder.build(new Identifier(MOD_ID,"group"), () -> new ItemStack(Registry.ITEM.get(new Identifier(MOD_ID, "quest_scroll"))));

    @Override
    public void onInitialize() {
		DataManager.init();
		GuildCommands.init();

		ServerNetworkManager.registerClientReceiver();
    }

	public static void info(String message){
        LOGGER.log(Level.INFO, message);
    }
}