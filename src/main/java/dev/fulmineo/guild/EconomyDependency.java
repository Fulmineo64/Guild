package dev.fulmineo.guild;


import com.epherical.octoecon.api.Currency;
import com.epherical.octoecon.api.Economy;
import com.epherical.octoecon.api.user.UniqueUser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class EconomyDependency {

	private Economy economy;

	public EconomyDependency(Economy economy) {
		this.economy = economy;
	}

	public String validateCurrency(String currency) {
		String addError = "";
		if (economy == null) {
			addError = currency + " No economy installed";
		} else if (economy.getCurrency(new Identifier(currency)) == null || economy.getDefaultCurrency() == null) {
			addError = currency + " Incorrect currency";
		}
		return addError;
	}

	public void giveReward(boolean expired, NbtCompound entry, ServerPlayerEntity player) {
		int count = entry.getInt("Count");
		if (expired) {
			int cnt1 = player.world.random.nextInt(count + 1);
			if (player.hasStatusEffect(StatusEffects.LUCK)) {
				int cnt2 = player.world.random.nextInt(count + 1);
				count = Math.max(cnt1, cnt2);
			} else {
				count = cnt1;
			}
		}
		Currency currency = economy.getCurrency(new Identifier(entry.getString("Name")));
		if (currency == null) {
			currency = economy.getDefaultCurrency();
		}
		UUID playerID = player.getUuid();
		UniqueUser user = economy.getOrCreatePlayerAccount(playerID);
		if (user != null) {
			user.depositMoney(currency, count, "Guild reward");
		}
	}
	@Environment(EnvType.CLIENT)
	public void addRewardName(ItemStack stack, NbtCompound entry) {
		Currency currency = economy.getCurrency(new Identifier(entry.getString("Name")));
		if (currency == null) {
			currency = economy.getDefaultCurrency();
		}
		int amount = entry.getInt("Count");
		if (amount > 1) {
			stack.setCustomName(currency.getCurrencyPluralName());
		} else {
			stack.setCustomName(currency.getCurrencySingularName());
		}
	}
}
