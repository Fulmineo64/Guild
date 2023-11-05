package dev.fulmineo.guild.item;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.ServerDataManager;
import dev.fulmineo.guild.data.ClientDataManager;
import dev.fulmineo.guild.data.GuildServerPlayerEntity;
import dev.fulmineo.guild.data.QuestProfession;
import dev.fulmineo.guild.data.QuestProfessionRequirement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
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
				if (((GuildServerPlayerEntity)user).getQuestProfessions().size() < Guild.CONFIG.getMaxProfessions()) {
					QuestProfession profession = ServerDataManager.professions.get(professionName);
					if (profession == null) {
						user.sendMessage(Text.translatable("profession.guild.invalid_profession", QuestProfession.getTranslatedText(professionName)), false);
						return TypedActionResult.fail(stack);
					}
					Map<String, Integer> professionsExp = ((GuildServerPlayerEntity)user).getProfessionExp();
					if (!profession.checkRequirements(professionsExp)) {
						user.sendMessage(Text.translatable("item.guild.profession_licence.missing_requirements", QuestProfession.getTranslatedText(professionName)), false);
						return TypedActionResult.fail(stack);
					}
					if (((GuildServerPlayerEntity)user).addQuestProfession(professionName)) {
						stack.damage(1, (LivingEntity)user, (Consumer<LivingEntity>)((p) -> { p.sendToolBreakStatus(hand); }));
						if (((GuildServerPlayerEntity)user).getQuestProfessions().size() == 1){
							user.sendMessage(Text.translatable("item.guild.profession_licence.introduction"), false);
						}
						user.sendMessage(Text.translatable("item.guild.profession_licence.licence.success", QuestProfession.getTranslatedText(profession.name)), false);
						ItemStack newStack = new ItemStack(Guild.QUEST_PROFESSION_RESIGNMENT_ITEM);
						NbtCompound tag = stack.getOrCreateNbt();
						tag.putString("Profession", nbt.getString("Profession"));
						user.getItemCooldownManager().set(Guild.QUEST_PROFESSION_LICENCE_ITEM, 10);
						user.getItemCooldownManager().set(Guild.QUEST_PROFESSION_RESIGNMENT_ITEM, 10);
						newStack.setNbt(tag);
						return TypedActionResult.success(newStack);
					} else {
						user.sendMessage(Text.translatable("item.guild.profession_licence.licence.fail", QuestProfession.getTranslatedText(profession.name)), false);
					}
				} else {
					user.sendMessage(Text.translatable("item.guild.profession_licence.too_many_professions", Guild.CONFIG.getMaxProfessions()), false);
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
			tooltip.add(Text.translatable("profession.profession").append(" ").append(QuestProfession.getTranslatedText(professionName)).formatted(Formatting.GOLD));
			QuestProfessionRequirement[] requirements = ClientDataManager.professionRequirements.get(professionName);
			if (requirements != null) {
				tooltip.add(Text.translatable("item.guild.profession_licence.requirements").formatted(Formatting.GREEN));
				for (QuestProfessionRequirement req: requirements) {
					MutableText text = req.profession != null ? QuestProfession.getTranslatedText(req.profession) : Text.translatable("item.guild.profession_licence.any_profession");
					if (req.level != null) {
						text = text.append(Text.translatable("item.guild.profession_licence.level"));
						if (req.level.min != null) {
							text = text.append(">= "+req.level.min+" ");
						}
						if (req.level.max != null) {
							text = text.append("<= "+req.level.max);
						}
					}
					tooltip.add(text.formatted(Formatting.GREEN));
				}
			}
			tooltip.add(Text.translatable("item.guild.profession_licence.description").formatted(Formatting.DARK_GRAY));
			tooltip.add(Text.translatable("item.guild.profession_licence.description2").formatted(Formatting.DARK_GRAY));
		}
	}
}
