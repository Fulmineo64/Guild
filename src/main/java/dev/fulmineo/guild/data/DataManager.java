package dev.fulmineo.guild.data;

import java.io.InputStream;
import java.util.HashMap;
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

	public static Map<String, QuestProfession> professions = new HashMap<>();

	public static void listen(){
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier("guild", "data");
			}

			@Override
			public void reload(ResourceManager manager) {
				Guild.info("reload");

				professions.clear();

				Map<String, QuestPool> pools = new HashMap<>();

				for(Identifier id : manager.findResources("quests/professions", path -> path.endsWith(".json"))) {
					Guild.info(id.toString());
					try(InputStream stream = manager.getResource(id).getInputStream()) {
						try {
							QuestProfession profession = (QuestProfession)GSON.fromJson(new String(stream.readAllBytes()), QuestProfession.class);
							professions.put(profession.name, profession);
						} catch (Exception e) {
							Guild.LOGGER.error((String)"Couldn't parse quest profession {}", (Object)id, (Object)e);
						}
					} catch(Exception e) {
						Guild.LOGGER.error("Error occurred while loading resource json " + id.toString(), e);
					}
				}

				for(Identifier id : manager.findResources("quests/pools", path -> path.endsWith(".json"))) {
					Guild.info(id.toString());
					try(InputStream stream = manager.getResource(id).getInputStream()) {
						try {
							QuestPool pool = (QuestPool)GSON.fromJson(new String(stream.readAllBytes()), QuestPool.class);
							pools.put(pool.name, pool);
						} catch (Exception e) {
							Guild.LOGGER.error((String)"Couldn't parse quest pool {}", (Object)id, (Object)e);
						}
					} catch(Exception e) {
						Guild.LOGGER.error("Error occurred while loading resource json " + id.toString(), e);
					}
				}

				for (Map.Entry<String, QuestProfession> entry : professions.entrySet()) {
					QuestProfession profession = entry.getValue();
					for (String taskId : profession.taskPools) {
						QuestPool pool = pools.get(taskId);
						if (pool == null) {
							Guild.LOGGER.error("Couldn't find task quest pool "+taskId+". Skipping.");
						} else {
							profession.addTaskPool(pool);
						}
					}

					for (String rewardId : profession.rewardPools) {
						QuestPool pool = pools.get(rewardId);
						if (pool == null) {
							Guild.LOGGER.error("Couldn't find reward quest pool "+rewardId+". Skipping.");
						} else {
							profession.addRewardPool(pool);
						}
					}
				}
			}
		});
	}
}
