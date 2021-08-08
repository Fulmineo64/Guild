package dev.fulmineo.guild.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestProfession {
	public String name;
	public String icon;
	public String[] taskPools;
	public String[] rewardPools;
	public List<QuestPoolData> tasks = new ArrayList<>();
	public List<QuestPoolData> rewards = new ArrayList<>();

	public void addTaskPool(QuestPool pool){
		tasks.addAll(Arrays.asList(pool.data));
	}

	public void addRewardPool(QuestPool pool){
		rewards.addAll(Arrays.asList(pool.data));
	}
}
