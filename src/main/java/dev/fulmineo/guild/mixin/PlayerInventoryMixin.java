package dev.fulmineo.guild.mixin;

import java.util.Iterator;

import com.google.common.collect.ImmutableList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.fulmineo.guild.item.QuestScrollItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {

	@Shadow
	public PlayerEntity player;
	@Shadow
	public DefaultedList<ItemStack> main;
	@Shadow
	public DefaultedList<ItemStack> offHand;

	@Inject(at = @At("HEAD"), method = "insertStack(Lnet/minecraft/item/ItemStack;)Z")
	public void insertStackMixin(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		if (this.player instanceof ServerPlayerEntity) {
			this.updateQuestItems(stack);
		}
	}

	public void updateQuestItems(ItemStack obtainedStack) {
		ImmutableList<DefaultedList<ItemStack>> mainAndOffhand = ImmutableList.of(this.main, this.offHand);
		Iterator<DefaultedList<ItemStack>> iterator = mainAndOffhand.iterator();
		while (iterator.hasNext()) {
			DefaultedList<ItemStack> defaultedList = (DefaultedList<ItemStack>) iterator.next();
			for (int i = 0; i < defaultedList.size(); ++i) {
				if (defaultedList.get(i).getItem() instanceof QuestScrollItem) {
					QuestScrollItem.updateItems(defaultedList.get(i), obtainedStack, this.player);
				}
			}
		}
	}
}
