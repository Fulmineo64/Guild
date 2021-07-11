package dev.fulmineo.guild.mixin;

import java.util.Iterator;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;

import dev.fulmineo.guild.item.QuestScrollItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

	public ServerPlayerEntityMixin(MinecraftServer server, ServerWorld world, GameProfile profile) {
		super(world, world.getSpawnPos(), world.getSpawnAngle(), profile);
	}

	public void onKilledOther(ServerWorld world, LivingEntity killedEntity) {
		super.onKilledOther(world, killedEntity);
		this.updateQuestEntities(killedEntity);
	}

	public void updateQuestEntities(LivingEntity killedEntity) {
		PlayerInventory inventory = this.getInventory();
		ImmutableList<DefaultedList<ItemStack>> mainAndOffhand = ImmutableList.of(inventory.main, inventory.offHand);
		Iterator<DefaultedList<ItemStack>> iterator = mainAndOffhand.iterator();
		while (iterator.hasNext()) {
			DefaultedList<ItemStack> defaultedList = (DefaultedList<ItemStack>) iterator.next();
			for (int i = 0; i < defaultedList.size(); ++i) {
				if (defaultedList.get(i).getItem() instanceof QuestScrollItem) {
					QuestScrollItem.updateEntities(defaultedList.get(i), killedEntity, this);
				}
			}
		}
	}
}
