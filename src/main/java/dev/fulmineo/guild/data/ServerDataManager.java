package dev.fulmineo.guild.data;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import dev.fulmineo.guild.Guild;

public class ServerDataManager {
	private static final Gson GSON = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
	.registerTypeAdapter(NbtCompound.class, new JsonDeserializer<NbtCompound>() {
		@Override
		public NbtCompound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return json.isJsonObject() ? (NbtCompound)this.jsonToNbt(json) : null;
		}

		public NbtElement jsonToNbt(JsonElement json) {
			if (json.isJsonArray()) {
				JsonArray arr = json.getAsJsonArray();
				if (arr.size() > 0 && arr.get(0).isJsonPrimitive() && arr.get(0).getAsString().startsWith("I;")) {
					int[] intArr = new int[arr.size()];
					intArr[0] = Integer.parseInt(arr.get(0).getAsString().replace("I;", ""));
					for (int i = 1; i < arr.size(); i++) {
						intArr[i] = arr.get(i).getAsInt();
					}
					return new NbtIntArray(intArr);
				} else {
					NbtList list = new NbtList();
					for (int i = 0; i < arr.size(); i++) {
						list.add(jsonToNbt(arr.get(i)));
					}
					return list;
				}
			} else if (json.isJsonObject()) {
				JsonObject obj = json.getAsJsonObject();
				NbtCompound nbt = new NbtCompound();
				for (Entry<String, JsonElement> entry: obj.entrySet()) {
					NbtElement elm = jsonToNbt(entry.getValue());
					if (elm != null) nbt.put(entry.getKey(), elm);
				}
				return nbt;
			} else if (json.isJsonPrimitive()) {
				String val = json.getAsString();
				if (Pattern.compile("^[\\d\\.]+$", Pattern.CASE_INSENSITIVE).matcher(val).find()) {
					if (val.contains(".")) {
						return NbtFloat.of(json.getAsFloat());
					} else {
						return NbtInt.of(json.getAsInt());
					}
				}
				return NbtString.of(val);
			} else {
				return null;
			}
		}
	})
    .create();

	public static Map<String, List<QuestLevel>> levels = new HashMap<>();
	public static Map<String, QuestProfession> professions = new HashMap<>();

	public static void init(){
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier(Guild.MOD_ID, "data");
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
							if (professions.containsKey(profession.name)) {
								professions.get(profession.name).merge(profession);
							} else {
								professions.put(profession.name, profession);
							}
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
							if (!pools.containsKey(pool.name)) {
								String invalidKey = pool.validate();
								if (invalidKey.length() == 0) {
									pools.put(pool.name, pool);
								} else {
									Guild.errors.add("Invalid key "+invalidKey.toString()+" in quest pool "+id.toString());
								}
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
