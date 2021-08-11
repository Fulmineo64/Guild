package dev.fulmineo.guild.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.fulmineo.guild.data.GuildServerPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class QuestProfessionLicence extends Item {
	public QuestProfessionLicence(Settings settings) {
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
			// TODO: Add required rank as a bonus check
			if (((GuildServerPlayerEntity)user).addQuestProfession(professionName)) {
				if (!user.getAbilities().creativeMode) {
					stack.decrement(1);
				}
			 	return TypedActionResult.success(stack);
			}
			return TypedActionResult.fail(stack);
        }
    }

	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		NbtCompound nbt = stack.getOrCreateNbt();
		String professionName = nbt.getString("Profession");
		if (professionName.length() > 0) {
			tooltip.add(new TranslatableText("profession.profession").append(" ").append(new TranslatableText("profession."+professionName.replace(":", "."))).formatted(Formatting.GOLD));
		}
	}
}
