package dev.fulmineo.guild.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class QuestHelper {
	private static int QUEST_GENERATION_TICKS = 3600;
	// private static int QUEST_GENERATION_TICKS = 1;
	private static int MAX_QUEST_TO_GENERATE = 10;
	private static int MAX_QUESTS_BY_PROFESSION = 7;

	public static void updateQuestEntities(PlayerEntity player, LivingEntity killedEntity) {
		List<Quest> guildQuests = ((GuildServerPlayerEntity)player).getAcceptedQuests();
		String entityIdentifier = EntityType.getId(killedEntity.getType()).toString();
		for (Quest quest: guildQuests) {
			quest.updateEntities(entityIdentifier, player);
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
			long lastGenTimeFrame = lastGenTime % QUEST_GENERATION_TICKS;
			long currentGenTimeFrame = time % QUEST_GENERATION_TICKS;

			int questsToGenerate = Math.min((int)(((time - currentGenTimeFrame) - (lastGenTime - lastGenTimeFrame)) / QUEST_GENERATION_TICKS), MAX_QUEST_TO_GENERATE);

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
				if (professionsQuest == null || professionsQuest.size() < MAX_QUESTS_BY_PROFESSION) {
					availableProfessions.add(profession);
				}
			}

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
				if (quests.size() == MAX_QUESTS_BY_PROFESSION) {
					availableProfessions.remove(professionIndex);
				}
			}
			if (i > 0) guildPlayer.setLastQuestGenTime(time);
		}
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
}
