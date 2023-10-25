package dev.fulmineo.guild.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableTextContent;

public class QuestProfession {
	public String name;
	public String icon;
	public String label;
	public int guildMasterLevel;
	public QuestProfessionRequirement[] requirements;
	public String levelsPool;
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

	public boolean checkRequirements(Map<String, Integer> professionsExp) {
		if (this.requirements != null) {
			for (QuestProfessionRequirement req: this.requirements) {
				if (req.level != null || req.profession != null) {
					if (req.profession != null) {
						// If the profession is specified, checks if it is present in the profession levels or if it is contained in the specified level range
						Integer exp = professionsExp.get(req.profession);
						if (exp == null) return false;
						if (req.level != null) {
							QuestProfession profession = ServerDataManager.professions.get(req.profession);
							if (profession == null) return false;
							List<QuestLevel> levels = ServerDataManager.levels.get(profession.levelsPool);
							if (levels == null) return false;
							int level = QuestHelper.getCurrentLevel(levels, exp) + 1;
							if (!req.level.contains(level)) return false;
						}
					} else {
						// If the profession is not specified, checks if at least one profession is in the desired level range
						for (Entry<String, Integer> entry: professionsExp.entrySet()) {
							String professionName = entry.getKey();
							QuestProfession profession = ServerDataManager.professions.get(professionName);
							if (profession != null) {
								Integer exp = professionsExp.get(professionName);
								if (exp == null) continue;
								List<QuestLevel> levels = ServerDataManager.levels.get(profession.levelsPool);
								if (levels == null) continue;
								int level = QuestHelper.getCurrentLevel(levels, exp) + 1;
								if (req.level.contains(level)) return true;
							}
						}
						return false;
					}
				}
			}
		}
		return true;
	}

	public void merge(QuestProfession professionToMerge) {
		if (this.name == null) this.name = professionToMerge.name;
		if (this.icon == null) this.icon = professionToMerge.icon;
		if (this.label == null) this.label = professionToMerge.label;
		if (this.guildMasterLevel == 0) this.guildMasterLevel = professionToMerge.guildMasterLevel;
		if (this.requirements == null) this.requirements = professionToMerge.requirements;
		if (this.levelsPool == null) this.levelsPool = professionToMerge.levelsPool;
		if (this.taskPools == null) this.taskPools = professionToMerge.taskPools;
		if (this.rewardPools == null) this.rewardPools = professionToMerge.rewardPools;

		List<String> taskPools = new ArrayList<>(Arrays.asList(this.taskPools));
		List<String> rewardPools = new ArrayList<>(Arrays.asList(this.rewardPools));
		for (String task: professionToMerge.taskPools) {
			if (!taskPools.contains(task)) {
				taskPools.add(task);
			}
		}
		for (String reward: professionToMerge.rewardPools) {
			if (!rewardPools.contains(reward)) {
				rewardPools.add(reward);
			}
		}
		this.taskPools = taskPools.toArray(new String[0]);
		this.rewardPools = rewardPools.toArray(new String[0]);
	}

	public static String getTranslationKey(String name) {
		return "profession."+name.replace(":", ".");
	}

	public static MutableText getTranslatedText(String name) {
		String label = ClientDataManager.professionsLabels.get(name);
		return label != null ? new LiteralTextContent(label) : new TranslatableTextContent(QuestProfession.getTranslationKey(name));
	}
}
