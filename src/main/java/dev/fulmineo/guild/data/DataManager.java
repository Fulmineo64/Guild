package dev.fulmineo.guild.data;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import dev.fulmineo.guild.Guild;

public class DataManager {
	private static final Gson GSON = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .create();

	public static Map<String, List<QuestLevel>> levels = new HashMap<>();
	public static Map<String, QuestProfession> professions = new HashMap<>();

	public static void init(){
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier("guild", "data");
			}

			@Override
			public void reload(ResourceManager manager) {
				Guild.errors.clear();
				levels.clear();
				professions.clear();

				Map<String, QuestPool> pools = new HashMap<>();

				for(Identifier id : manager.findResources("quests/levels", path -> path.endsWith(".json"))) {
					try(InputStream stream = manager.getResource(id).getInputStream()) {
						try {
							QuestLevels data = (QuestLevels)GSON.fromJson(new String(stream.readAllBytes()), QuestLevels.class);
							levels.put(data.name, data.levels);
						} catch (Exception e) {
							Guild.errors.add("Couldn't parse quest level "+id.toString());
						}
					} catch(Exception e) {
						Guild.errors.add("Error occurred while loading resource json "+id.toString());
					}
				}

				for(Identifier id : manager.findResources("quests/professions", path -> path.endsWith(".json"))) {
					try(InputStream stream = manager.getResource(id).getInputStream()) {
						try {
							QuestProfession profession = (QuestProfession)GSON.fromJson(new String(stream.readAllBytes()), QuestProfession.class);
							professions.put(profession.name, profession);
						} catch (Exception e) {
							Guild.errors.add("Couldn't parse quest profession "+id.toString());
						}
					} catch(Exception e) {
						Guild.errors.add("Error occurred while loading resource json "+id.toString());
					}
				}

				for(Identifier id : manager.findResources("quests/pools", path -> path.endsWith(".json"))) {
					try(InputStream stream = manager.getResource(id).getInputStream()) {
						try {
							QuestPool pool = (QuestPool)GSON.fromJson(new String(stream.readAllBytes()), QuestPool.class);
							String invalidKey = pool.validate();
							if (invalidKey.length() == 0) {
								pools.put(pool.name, pool);
							} else {
								Guild.errors.add("Invalid key "+invalidKey.toString()+" in quest pool "+id.toString());
							}
						} catch (Exception e) {
							Guild.errors.add("Couldn't parse quest pool "+id.toString());
						}
					} catch(Exception e) {
						Guild.errors.add("Error occurred while loading resource json "+id.toString());
					}
				}

				for (Map.Entry<String, QuestProfession> entry : professions.entrySet()) {
					QuestProfession profession = entry.getValue();
					if (profession.taskPools == null) {
						Guild.errors.add("Missing required property taskPools in profession "+entry.getKey());
					} else {
						for (String taskId : profession.taskPools) {
							QuestPool pool = pools.get(taskId);
							if (pool == null) {
								Guild.errors.add("Couldn't find task quest pool "+taskId+ " required by profession "+entry.getKey());
							} else {
								profession.addTaskPool(pool);
							}
						}
					}

					if (profession.rewardPools == null) {
						Guild.errors.add("Missing required property rewardPools in profession "+entry.getKey());
					} else {
						for (String rewardId : profession.rewardPools) {
							QuestPool pool = pools.get(rewardId);
							if (pool == null) {
								Guild.errors.add("Couldn't find reward quest pool "+rewardId+ " required by profession "+entry.getKey());
							} else {
								profession.addRewardPool(pool);
							}
						}
					}

					if (profession.levelsPool == null) {
						Guild.errors.add("Missing required property levelsPool in profession "+entry.getKey());
					} else {
						if (levels.get(profession.levelsPool) == null) {
							Guild.errors.add("Couldn't find levels pool "+profession.levelsPool+ " required by profession "+entry.getKey());
						}
					}
				}

				VillagerData.refreshTrades(professions);
			}
		});
	}
}
