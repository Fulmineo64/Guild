package dev.fulmineo.guild.config;

import dev.fulmineo.guild.Guild;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = Guild.MOD_ID)
public class GuildConfig implements ConfigData {

    public int expirationTicks = 108000;
	public int questGenerationTicks = 3600;
	public int maxProfessions = 7;
	public int maxAcceptedQuests = 7;
	public int maxQuestsPerProfession = 7;
	public int maxQuestsPerGeneration = 10;
	public boolean displayUnlockedPools = true;

    public static GuildConfig getInstance() {
        return AutoConfig.getConfigHolder(GuildConfig.class).getConfig();
    }

	public int getMaxProfessions() {
		return Math.min(this.maxProfessions, 7);
	}

	public int getMaxAcceptedQuests() {
		return Math.min(this.maxAcceptedQuests, 7);
	}

	public int getMaxQuestsPerProfession() {
		return Math.min(this.maxQuestsPerProfession, 7);
	}
}
