package dev.fulmineo.guild.item;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.DataManager;
import dev.fulmineo.guild.data.GuildServerPlayerEntity;
import dev.fulmineo.guild.data.QuestProfession;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
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
			if (user.isSneaking()) {
				ItemStack newStack = new ItemStack(Guild.QUEST_PROFESSION_RESIGNMENT_ITEM);
				NbtCompound tag = stack.getOrCreateNbt();
				tag.putString("Profession", nbt.getString("Profession"));
				user.getItemCooldownManager().set(Guild.QUEST_PROFESSION_LICENCE_ITEM, 10);
				user.getItemCooldownManager().set(Guild.QUEST_PROFESSION_RESIGNMENT_ITEM, 10);
				newStack.setNbt(tag);
				return TypedActionResult.success(newStack);
			} else {
				// TODO: Add required rank as a bonus check
				if (((GuildServerPlayerEntity)user).getProfessions().size() <= 7) {
					QuestProfession profession = DataManager.professions.get(professionName);
					if (profession == null) {
						user.sendMessage(new TranslatableText("profession.guild.invalid_profession", professionName), false);
						return TypedActionResult.fail(stack);
					}
					if (((GuildServerPlayerEntity)user).addQuestProfession(professionName)) {
						stack.damage(1, (LivingEntity)user, (Consumer<LivingEntity>)((p) -> { p.sendToolBreakStatus(hand); }));
						if (((GuildServerPlayerEntity)user).getProfessions().size() == 1){
							user.sendMessage(new TranslatableText("item.guild.profession_licence.introduction"), false);
						}
						user.sendMessage(new TranslatableText("item.guild.profession_licence.licence.success", profession.getTranslatedName()), false);
						ItemStack newStack = new ItemStack(Guild.QUEST_PROFESSION_RESIGNMENT_ITEM);
						NbtCompound tag = stack.getOrCreateNbt();
						tag.putString("Profession", nbt.getString("Profession"));
						user.getItemCooldownManager().set(Guild.QUEST_PROFESSION_LICENCE_ITEM, 10);
						user.getItemCooldownManager().set(Guild.QUEST_PROFESSION_RESIGNMENT_ITEM, 10);
						newStack.setNbt(tag);
						return TypedActionResult.success(newStack);
					} else {
						user.sendMessage(new TranslatableText("item.guild.profession_licence.licence.fail", profession.getTranslatedName()), false);
					}
				} else {
					user.sendMessage(new TranslatableText("item.guild.profession_licence.too_many_professions", 7), false);
				}
			}
			return TypedActionResult.fail(stack);
        }
    }

	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		NbtCompound nbt = stack.getOrCreateNbt();
		String professionName = nbt.getString("Profession");
		if (professionName.length() > 0) {
			tooltip.add(new TranslatableText("profession.profession").append(" ").append(new TranslatableText(QuestProfession.getTranslationKey(professionName))).formatted(Formatting.GOLD));
			tooltip.add(new TranslatableText("item.guild.profession_licence.description").formatted(Formatting.DARK_GRAY));
			tooltip.add(new TranslatableText("item.guild.profession_licence.description2").formatted(Formatting.DARK_GRAY));
		}
	}
}
