package dev.fulmineo.guild.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;

public class GuildMasterEntity extends VillagerEntity {

	public GuildMasterEntity(EntityType<? extends GuildMasterEntity> entityType, World world) {
		this(entityType, world, VillagerType.PLAINS);
	}

	public GuildMasterEntity(EntityType<? extends GuildMasterEntity> entityType, World world, VillagerType type) {
		super(entityType, world, type);
	}

	public void tick() {
		super.tick();
		// TODO: Update up to 5 quest blocks without moving or set the various blocks as Point of Interests
	}
}
