package dev.fulmineo.guild.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.fulmineo.guild.data.ServerDataManager;
import dev.fulmineo.guild.data.GuildServerPlayerEntity;
import dev.fulmineo.guild.data.QuestProfession;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
			QuestProfession profession = ServerDataManager.professions.get(professionName);
			if (profession == null) {
				user.sendMessage(Text.translatable("profession.guild.invalid_profession", professionName), false);
				return TypedActionResult.fail(stack);
			}
			if (((GuildServerPlayerEntity)user).removeQuestProfession(professionName)) {
				user.sendMessage(Text.translatable("item.guild.profession_resignment.resignment.success", QuestProfession.getTranslatedText(professionName)), false);
				return TypedActionResult.success(new ItemStack(Items.AIR));
			} else {
				user.sendMessage(Text.translatable("item.guild.profession_resignment.resignment.fail", QuestProfession.getTranslatedText(professionName)), false);
			}
			return TypedActionResult.fail(stack);
        }
    }

	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		NbtCompound nbt = stack.getOrCreateNbt();
		String professionName = nbt.getString("Profession");
		if (professionName.length() > 0) {
			tooltip.add(Text.translatable("profession.profession").append(" ").append(QuestProfession.getTranslatedText(professionName)).formatted(Formatting.GOLD));
			tooltip.add(Text.translatable("item.guild.profession_resignment.description").formatted(Formatting.DARK_GRAY));
		}
	}
}
