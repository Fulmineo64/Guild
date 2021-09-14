package dev.fulmineo.guild.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.fulmineo.guild.data.QuestHelper;
import net.minecraft.advancement.criterion.CuredZombieVillagerCriterion;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(CuredZombieVillagerCriterion.class)
public class CuredZombieVillagerCriterionMixin {
	@Inject(at = @At("TAIL"), method = "trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/mob/ZombieEntity;Lnet/minecraft/entity/passive/VillagerEntity;)V")
	public void trigger(ServerPlayerEntity player, ZombieEntity zombie, VillagerEntity villager, CallbackInfo info) {
		QuestHelper.updateQuestCure(player, zombie);
	}
}
