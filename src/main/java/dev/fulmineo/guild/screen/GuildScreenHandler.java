package dev.fulmineo.guild.screen;

import java.util.List;
import java.util.Map;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.Quest;
import dev.fulmineo.guild.data.QuestHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class GuildScreenHandler extends ScreenHandler {
	// private final Inventory inventory;

	public Map<String, List<Quest>> availableQuests;

	public GuildScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf){
        this(syncId, playerInventory, QuestHelper.fromMapNbt(buf.readNbt()));
    }

    public GuildScreenHandler(int syncId, PlayerInventory playerInventory, Map<String, List<Quest>> availableQuests) {
        super(Guild.GUILD_SCREEN_HANDLER, syncId);
		this.availableQuests = availableQuests;

		int k;
		for(k = 0; k < 3; ++k) {
			for(int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + k * 9 + 9, 108 + j * 18, 84 + k * 18));
			}
		}

		for(k = 0; k < 9; ++k) {
		   this.addSlot(new Slot(playerInventory, k, 108 + k * 18, 142));
		}
	}

	public boolean canUse(PlayerEntity player) {
		// TODO: Check if the player has at least 1 guild
        return true;
    }
}
