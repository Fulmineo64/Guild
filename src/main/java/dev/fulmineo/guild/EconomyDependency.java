package dev.fulmineo.guild;


import com.epherical.octoecon.api.Currency;
import com.epherical.octoecon.api.Economy;
import com.epherical.octoecon.api.user.UniqueUser;
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

	public void giveReward(int count, NbtCompound entry, ServerPlayerEntity player) {
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

	public String getCurrencyName(String name, int count) {
		Currency currency = economy.getCurrency(new Identifier(name));
		if (currency == null) {
			currency = economy.getDefaultCurrency();
		}
		if (count > 1) {
			return currency.getCurrencyPluralName().asString();
		} else {
			return currency.getCurrencySingularName().asString();
		}
	}
}
