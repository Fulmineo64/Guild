package dev.fulmineo.guild.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dev.fulmineo.guild.Guild;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class QuestHelper {
	public static void updateQuestSlay(PlayerEntity player, LivingEntity killedEntity) {
		List<Quest> guildQuests = ((GuildServerPlayerEntity)player).getAcceptedQuests();
		String entityIdentifier = EntityType.getId(killedEntity.getType()).toString();
		for (Quest quest: guildQuests) {
			quest.updateSlay(entityIdentifier, killedEntity, player);
		}
	}

	public static void updateQuestCure(PlayerEntity player, LivingEntity curedEntity) {
		List<Quest> guildQuests = ((GuildServerPlayerEntity)player).getAcceptedQuests();
		String entityIdentifier = EntityType.getId(curedEntity.getType()).toString();
		for (Quest quest: guildQuests) {
			quest.updateCure(entityIdentifier, curedEntity, player);
		}
	}

	public static void updateQuestSummon(PlayerEntity player, LivingEntity summonedEntity) {
		List<Quest> guildQuests = ((GuildServerPlayerEntity)player).getAcceptedQuests();
		String entityIdentifier = EntityType.getId(summonedEntity.getType()).toString();
		for (Quest quest: guildQuests) {
			quest.updateSummon(entityIdentifier, summonedEntity, player);
		}
	}

	public static NbtCompound writeNbt(NbtCompound nbt, List<Quest> guildQuests) {
		NbtList questData = new NbtList();
		for (Quest quest: guildQuests) {
			questData.add(quest.writeNbt(new NbtCompound()));
		}
		nbt.put("Quests", questData);
		return nbt;
	}

	public static List<Quest> fromNbt(NbtCompound nbt) {
		NbtList questData = nbt.getList("Quests", NbtElement.COMPOUND_TYPE);
		List<Quest> guildQuests = new ArrayList<>();
		for (NbtElement data: questData){
			guildQuests.add(Quest.fromNbt((NbtCompound)data));
		}
		return guildQuests;
	}

	public static void refreshAvailableQuests(List<QuestProfession> professions, PlayerEntity player) {
		long time = player.world.getTime();
		GuildServerPlayerEntity guildPlayer = (GuildServerPlayerEntity)player;
		Map<String, List<Quest>> availableQuests = guildPlayer.getAvailableQuests();

		if (professions.size() > 0) {
			long lastGenTime = guildPlayer.getLastQuestGenTime();
			long lastGenTimeFrame = lastGenTime % Guild.QUEST_GENERATION_TICKS;
			long currentGenTimeFrame = time % Guild.QUEST_GENERATION_TICKS;

			int questsToGenerate = Math.min((int)(((time - currentGenTimeFrame) - (lastGenTime - lastGenTimeFrame)) / Guild.QUEST_GENERATION_TICKS), Guild.MAX_QUEST_TO_GENERATE);

			for (List<Quest> quests: availableQuests.values()) {
				Iterator<Quest> iterator = quests.iterator();
				while (iterator.hasNext()) {
					if (iterator.next().nbt.getLong("AvailableUntil") < time) {
						iterator.remove();
					}
				}
			}

			List<QuestProfession> availableProfessions = new ArrayList<>();
			for (QuestProfession profession: professions) {
				List<Quest> professionsQuest = availableQuests.get(profession.name);
				if (professionsQuest == null || professionsQuest.size() < Guild.MAX_QUESTS_BY_PROFESSION) {
					availableProfessions.add(profession);
				}
			}

			int generated = generateQuests(player, availableProfessions, questsToGenerate);
			if (generated > 0) guildPlayer.setLastQuestGenTime(time);
		}
	}

	public static int generateQuests(PlayerEntity player, List<QuestProfession> availableProfessions, int questsToGenerate) {
		GuildServerPlayerEntity guildPlayer = (GuildServerPlayerEntity)player;
		Map<String, List<Quest>> availableQuests = guildPlayer.getAvailableQuests();
		int i;
		for (i = 0; i < questsToGenerate; i++){
			if (availableProfessions.size() == 0) break;
			int professionIndex = player.world.random.nextInt(availableProfessions.size());
			QuestProfession profession = availableProfessions.get(professionIndex);
			List<Quest> quests;
			if (availableQuests.containsKey(profession.name)) {
				quests = availableQuests.get(profession.name);
			} else {
				quests = new ArrayList<>();
			}
			quests.add(Quest.create(profession, player));
			availableQuests.put(profession.name, quests);
			if (quests.size() == Guild.MAX_QUESTS_BY_PROFESSION) {
				availableProfessions.remove(professionIndex);
			}
		}
		return i;
	}

    public static NbtCompound writeMapNbt(NbtCompound nbt, Map<String, List<Quest>> availableQuests) {
		NbtCompound professionsData = new NbtCompound();
        for (Entry<String, List<Quest>> professionQuests: availableQuests.entrySet()) {
			NbtList questsData = new NbtList();
			for (Quest quest: professionQuests.getValue()) {
				questsData.add(quest.writeNbt(new NbtCompound()));
			}
			professionsData.put(professionQuests.getKey(), questsData);
		}
		nbt.put("QuestMap", professionsData);
		return nbt;
    }

	public static Map<String, List<Quest>> fromMapNbt(NbtCompound nbt) {
		Map<String, List<Quest>> availableQuests = new HashMap<>();
		NbtCompound professionsData = nbt.getCompound("QuestMap");
		for (String professionName: professionsData.getKeys()) {
			List<Quest> quests = new ArrayList<>();
			NbtList questsData = professionsData.getList(professionName, NbtElement.COMPOUND_TYPE);
			for (NbtElement questData: questsData) {
				quests.add(Quest.fromNbt((NbtCompound)questData));
			}
			availableQuests.put(professionName, quests);
		}
		return availableQuests;
	}

	public static int getCurrentLevel(List<QuestLevel> levels, int exp) {
		for (int i = levels.size() - 1; i >= 0; i--) {
			if (levels.get(i).exp <= exp) {
				return i;
			}
		}
		return levels.size() - 1;
	}

	public static NbtCompound writeProfessionData(NbtCompound nbt, GuildServerPlayerEntity guildPlayer, QuestProfession profession) {
		List<QuestLevel> levels = ServerDataManager.levels.get(profession.levelsPool);
		int exp = guildPlayer.getProfessionExp(profession.name);
		int level = QuestHelper.getCurrentLevel(levels, exp);
		nbt.putInt("Level", level);
		boolean levelMax = level == levels.size() - 1;
		if (levelMax) {
			nbt.putInt("LevelPerc", 100);
		} else {
			QuestLevel currentLevel = levels.get(level);
			QuestLevel nextLevel = levels.get(level+1);
			nbt.putInt("LevelPerc", (int)(((float)(exp - currentLevel.exp) / (float)(nextLevel.exp - currentLevel.exp)) * 100));
		}
		return nbt;
	}
}
