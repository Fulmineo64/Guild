package dev.fulmineo.guild.mixin;

import org.spongepowered.asm.mixin.Mixin;

import dev.fulmineo.guild.data.QuestHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

@Mixin(TameableEntity.class)
public abstract class TameableEntityMixin extends AnimalEntity implements Tameable {

	protected TameableEntityMixin(EntityType<? extends TameableEntity> entityType, World world) {
		super(entityType, world);
	}

	public boolean onKilledOther(ServerWorld world, LivingEntity killedEntity) {
		boolean result = super.onKilledOther(world, killedEntity);

		LivingEntity owner = this.getOwner();
		if (owner != null && owner instanceof PlayerEntity) {
			QuestHelper.updateQuestSlay((PlayerEntity)owner, killedEntity);
		}
		return result;
	}

}
