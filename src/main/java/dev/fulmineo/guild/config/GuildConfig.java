package dev.fulmineo.guild.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "guild")
public class GuildConfig implements ConfigData {

    public int expirationTicks = 108000;
	public int questGenerationTicks = 3600;
	public int maxQuestsPerGeneration = 10;
	public boolean displayUnlockedPools = true;

    public static GuildConfig getInstance() {
        return AutoConfig.getConfigHolder(GuildConfig.class).getConfig();
    }

}