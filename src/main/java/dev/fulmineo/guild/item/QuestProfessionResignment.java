package dev.fulmineo.guild.item;

import dev.fulmineo.guild.data.GuildServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class QuestProfessionResignment extends Item {
	public QuestProfessionResignment(Settings settings) {
		super(settings);
	}

	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.success(stack);
		} else {
			NbtCompound nbt = stack.getOrCreateNbt();
			String professionName = nbt.getString("Profession");
			boolean removed = ((GuildServerPlayerEntity)user).removeQuestProfession(professionName);
			if (removed) return TypedActionResult.success(new ItemStack(Items.AIR));
			return TypedActionResult.fail(stack);
        }
    }
}
