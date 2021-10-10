package dev.fulmineo.guild.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import dev.fulmineo.guild.data.QuestHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

@Mixin(TameableEntity.class)
public abstract class TameableEntityMixin extends AnimalEntity {

	@Shadow
    public LivingEntity getOwner() { return null; }

	protected TameableEntityMixin(EntityType<? extends TameableEntity> entityType, World world) {
		super(entityType, world);
	}

	public void onKilledOther(ServerWorld world, LivingEntity killedEntity) {
		super.onKilledOther(world, killedEntity);

		LivingEntity owner = this.getOwner();
		if (owner != null && owner instanceof PlayerEntity) {
			QuestHelper.updateQuestSlay((PlayerEntity)owner, killedEntity);
		}
	}

}
