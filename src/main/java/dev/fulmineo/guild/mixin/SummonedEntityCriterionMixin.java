package dev.fulmineo.guild.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.fulmineo.guild.data.QuestHelper;
import net.minecraft.advancement.criterion.SummonedEntityCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(SummonedEntityCriterion.class)
public class SummonedEntityCriterionMixin {
	@Inject(at = @At("TAIL"), method = "trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity;)V")
	public void trigger(ServerPlayerEntity player, Entity entity, CallbackInfo info) {
		if (entity instanceof LivingEntity) {
			QuestHelper.updateQuestSummon(player, (LivingEntity)entity);
		}
	}
}
