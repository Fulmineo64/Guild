package dev.fulmineo.guild;

import dev.fulmineo.guild.client.KeyBindManager;
import dev.fulmineo.guild.screen.GuildScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class GuildClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(Guild.GUILD_SCREEN_HANDLER, GuildScreen::new);
		KeyBindManager.init();
    }
}