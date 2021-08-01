package dev.fulmineo.guild.client;

import org.lwjgl.glfw.GLFW;

import dev.fulmineo.guild.network.ClientNetworkManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class KeyBindManager {
	private static KeyBinding guildScreenKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
		"key.guild.guild_screen", // The translation key of the keybinding's name
		InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
		GLFW.GLFW_KEY_K, // The keycode of the key
		"category.guild.guild" // The translation key of the keybinding's category.
	));

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (guildScreenKeyBinding.wasPressed()) {
				ClientNetworkManager.openGuildScreen();
			}
		});
	}
}
