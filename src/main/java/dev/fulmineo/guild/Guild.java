package dev.fulmineo.guild;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
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
import dev.fulmineo.guild.item.QuestProfessionLicence;
import dev.fulmineo.guild.item.QuestProfessionResignment;
import dev.fulmineo.guild.network.ServerNetworkManager;
import dev.fulmineo.guild.screen.QuestsScreenHandler;

public class Guild implements ModInitializer {

	public static Logger LOGGER = LogManager.getLogger();

    // Identifiers

    public static final String MOD_ID = "guild";
	public static final Identifier OPEN_QUESTS_SCREEN_PACKET_ID = new Identifier(MOD_ID, "quest_screen_packet");
    public static final Identifier ACCEPT_QUEST_PACKET_ID = new Identifier(MOD_ID, "accept_quest_packet");
    public static final Identifier TRY_COMPLETE_QUEST_PACKET_ID = new Identifier(MOD_ID, "complete_quest_packet");
    public static final Identifier DELETE_ACCEPTED_QUEST_PACKET_ID = new Identifier(MOD_ID, "delete_accepted_quest_packet");
    public static final Identifier DELETE_AVAILABLE_QUEST_PACKET_ID = new Identifier(MOD_ID, "delete_available_quest_packet");
    public static final ScreenHandlerType<QuestsScreenHandler> QUESTS_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID, "quest_screen"), QuestsScreenHandler::new);
	public static final ItemGroup GROUP = FabricItemGroupBuilder.build(new Identifier(MOD_ID,"group"), () -> new ItemStack(Registry.ITEM.get(new Identifier(MOD_ID, "quest_scroll"))));

	public static final String[] SCREEN_TITLES = {"screen.guild.quest"};

	// Blocks

	public static final Block GUILD_MASTER_TABLE = new Block(AbstractBlock.Settings.copy(Blocks.CARTOGRAPHY_TABLE));

	// Items

	public static final Item GUILD_MASTER_TABLE_ITEM = new BlockItem(GUILD_MASTER_TABLE, new Item.Settings().group(GROUP));

    public static final Item QUEST_PROFESSION_LICENCE_ITEM = new QuestProfessionLicence(new FabricItemSettings());
    public static final Item QUEST_PROFESSION_RESIGNMENT_ITEM = new QuestProfessionResignment(new FabricItemSettings());

    @Override
    public void onInitialize() {
		// Blocks

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "guild_master_table"), GUILD_MASTER_TABLE);

		// Items

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "guild_master_table"), GUILD_MASTER_TABLE_ITEM);

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "profession_licence"), QUEST_PROFESSION_LICENCE_ITEM);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "profession_resignment"), QUEST_PROFESSION_RESIGNMENT_ITEM);

		// Data

		DataManager.init();
		GuildCommands.init();

		ServerNetworkManager.registerClientReceiver();
    }

	public static void info(String message){
        LOGGER.log(Level.INFO, message);
    }
}