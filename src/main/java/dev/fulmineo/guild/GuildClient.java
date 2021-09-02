package dev.fulmineo.guild;

import dev.fulmineo.guild.client.KeyBindManager;
import dev.fulmineo.guild.screen.QuestsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class GuildClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(Guild.QUESTS_SCREEN_HANDLER, QuestsScreen::new);
		KeyBindManager.init();
    }
}