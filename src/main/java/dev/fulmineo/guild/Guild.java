package dev.fulmineo.guild;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.fulmineo.guild.command.GuildCommands;
import dev.fulmineo.guild.data.DataManager;
import dev.fulmineo.guild.item.QuestScrollItem;

public class Guild implements ModInitializer {

	public static Logger LOGGER = LogManager.getLogger();

    // Identifiers

    public static final String MOD_ID = "guild";

	// Items

	public static final ItemGroup GROUP = FabricItemGroupBuilder.build(new Identifier(MOD_ID,"group"), () -> new ItemStack(Registry.ITEM.get(new Identifier(MOD_ID, "quest_scroll"))));

	public static final Item QUEST_SCROLL = new QuestScrollItem(new FabricItemSettings().group(GROUP).maxCount(1));

    @Override
    public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "quest_scroll"), QUEST_SCROLL);

		DataManager.listen();
		GuildCommands.init();
    }

	public static void info(String message){
        LOGGER.log(Level.INFO, message);
    }
}