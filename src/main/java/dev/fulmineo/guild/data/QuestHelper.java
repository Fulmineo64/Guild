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
	private static int QUEST_GENERATION_TICKS = 6000;
	private static int MAX_QUEST_TO_GENERATE = 10;

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

	/*public static List<Quest> generateQuests(QuestProfession profession, int min, int max){
		int amount = min + (new Random()).nextInt(max - min);
		List<Quest> guildQuests = new ArrayList<>();
		for (int i=0; i < amount; i++){
			guildQuests.add(Quest.create(profession));
		}
		return guildQuests;
	}*/

	public static void refreshAvailableQuests(List<QuestProfession> professions, PlayerEntity player) {
		long time = player.world.getTime();
		GuildServerPlayerEntity guildPlayer = (GuildServerPlayerEntity)player;
		Map<String, List<Quest>> availableQuests = guildPlayer.getAvailableQuests();
		for (Entry<String, List<Quest>> professionQuests: availableQuests.entrySet()) {
			List<Quest> quests = professionQuests.getValue();
			for (Iterator<Quest> iterator = quests.iterator(); iterator.hasNext();) {
				Quest quest = iterator.next();
				if (quest.isExpired(time)) {
					iterator.remove();
				}
			}
		}

		if (availableQuests.size() < 7) {
			long lastGenTime = guildPlayer.getLastQuestGenTime();
			long lastGenTimeFrame = lastGenTime % QUEST_GENERATION_TICKS;
			long currentGenTimeFrame = time % QUEST_GENERATION_TICKS;

			int questsToGenerate = Math.min((int)(((time - currentGenTimeFrame) - (lastGenTime - lastGenTimeFrame)) / QUEST_GENERATION_TICKS), MAX_QUEST_TO_GENERATE);
			if (questsToGenerate > 7 - availableQuests.size()) {
				questsToGenerate = 7 - availableQuests.size();
			}
			for (int i = 0; i < questsToGenerate; i++){
				int professionIndex = player.world.random.nextInt(professions.size());
				QuestProfession profession = professions.get(professionIndex);
				List<Quest> quests;
				if (availableQuests.containsKey(profession.name)) {
					quests = availableQuests.get(profession.name);
				} else {
					quests = new ArrayList<>();
				}
				quests.add(Quest.create(profession, time));
				availableQuests.put(profession.name, quests);
			}

			guildPlayer.setLastQuestGenTime(time);
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
		if (exp >= levels.get(levels.size()-1).exp) {
			return levels.size();
		}
		for (int i = 0; i < levels.size(); i++) {
			if (levels.get(i).exp <= exp) {
				return i;
			}
		}
		return -1;
	}
}
