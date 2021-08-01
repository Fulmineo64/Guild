package dev.fulmineo.guild.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {

	@Shadow
	public PlayerEntity player;
	@Shadow
	public DefaultedList<ItemStack> main;
	@Shadow
	public DefaultedList<ItemStack> offHand;

	// TODO: Rework this

	/*@Inject(at = @At("HEAD"), method = "insertStack(Lnet/minecraft/item/ItemStack;)Z")
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
	}*/
}
