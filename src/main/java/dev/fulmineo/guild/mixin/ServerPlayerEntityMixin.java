package dev.fulmineo.guild.mixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.fulmineo.guild.data.ServerDataManager;
import dev.fulmineo.guild.data.GuildServerPlayerEntity;
import dev.fulmineo.guild.data.Quest;
import dev.fulmineo.guild.data.QuestHelper;
import dev.fulmineo.guild.data.QuestProfession;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements GuildServerPlayerEntity {

	protected List<Quest> acceptedQuests = new ArrayList<>();
	protected Map<String, List<Quest>> availableQuests = new HashMap<>();
	protected long lastQuestGenTime = getWorld().getTime();
	protected List<String> professions = new ArrayList<>();
	protected Map<String, Integer> professionsExp = new HashMap<>();

	public ServerPlayerEntityMixin(MinecraftServer server, ServerWorld world, GameProfile profile) {
		super(world, world.getSpawnPos(), world.getSpawnAngle(), profile);
	}

	public boolean onKilledOther(ServerWorld world, LivingEntity killedEntity) {
		boolean result = super.onKilledOther(world, killedEntity);
		QuestHelper.updateQuestSlay(this, killedEntity);
		return result;
	}

	public List<Quest> getAcceptedQuests() {
		return this.acceptedQuests;
	}

	public void acceptQuest(Quest quest) {
		this.acceptedQuests.add(quest);
	}

	public Map<String, List<Quest>> getAvailableQuests() {
		return this.availableQuests;
	}

	public void setAvailableQuests(Map<String, List<Quest>> availableQuests) {
		this.availableQuests = availableQuests;
	}

	public void addAvailableQuest(String professionName, Quest quest) {
		List<Quest> quests = new ArrayList<>();
		if (this.availableQuests.containsKey(professionName)) {
			quests = this.availableQuests.get(professionName);
		}
		quests.add(quest);
		this.availableQuests.put(professionName, quests);
	}

	public long getLastQuestGenTime() {
		return this.lastQuestGenTime;
	}

	public void setLastQuestGenTime(long time) {
		this.lastQuestGenTime = time;
	}

	public List<String> getProfessions() {
		return this.professions;
	}

	public List<QuestProfession> getQuestProfessions() {
		List<QuestProfession> professions = new ArrayList<>();
		Iterator<String> iterator = this.professions.iterator();
		while (iterator.hasNext()) {
			String professionName = iterator.next();
			QuestProfession profession = ServerDataManager.professions.get(professionName);
			if (profession == null) {
				iterator.remove();
			} else {
				professions.add(profession);
			}
		}
		return professions;
	}

	public boolean addQuestProfession(String professionName) {
		boolean found = this.professions.contains(professionName);
		if (!found) this.professions.add(professionName);
		return !found;
	}

	public boolean removeQuestProfession(String professionName) {
		boolean found = this.professions.contains(professionName);
		if (found) this.professions.remove(professionName);
		return found;
	}

	public Map<String, Integer> getProfessionExp() {
		return this.professionsExp;
	}

	public int getProfessionExp(String professionName) {
		Integer val = this.professionsExp.get(professionName);
		return val != null ? val : 0;
	}

	public void setProfessionExp(String professionName, int exp) {
		this.professionsExp.put(professionName, exp);
	}

	public void resetQuestsAndProfessions() {
		this.clearQuests();
		this.professions.clear();
		this.professionsExp.clear();
	}

	public void clearQuests() {
		this.acceptedQuests.clear();
		this.availableQuests.clear();
	}

	@Inject(at = @At("TAIL"), method = "copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V")
	public void copyFromMixin(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo info) {
		GuildServerPlayerEntity guildPlayer = (GuildServerPlayerEntity)oldPlayer;
		this.lastQuestGenTime = guildPlayer.getLastQuestGenTime();
		this.acceptedQuests = guildPlayer.getAcceptedQuests();
		this.availableQuests = guildPlayer.getAvailableQuests();
		this.professions = guildPlayer.getProfessions();
		this.professionsExp = guildPlayer.getProfessionExp();
	}

	@Inject(at = @At("TAIL"), method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
		QuestHelper.writeMapNbt(nbt, this.availableQuests);
		QuestHelper.writeNbt(nbt, this.acceptedQuests);
		NbtList professions = new NbtList();
		for (String profession: this.professions) {
			professions.add(NbtString.of(profession));
		}
		nbt.put("Professions", professions);
		NbtCompound professionsExp = new NbtCompound();
		for (Entry<String, Integer> entry: this.professionsExp.entrySet()) {
			Integer val = entry.getValue();
			professionsExp.putInt(entry.getKey(), val != null ? val : 0);
		}
		nbt.put("ProfessionsExp", professionsExp);
	}

	@Inject(at = @At("TAIL"), method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
		this.availableQuests = QuestHelper.fromMapNbt(nbt);
		this.acceptedQuests = QuestHelper.fromNbt(nbt);
		// Workaround for mods like LevelZ that call this function multiple times
		this.professions.clear();
		this.professionsExp.clear();
		NbtList professions = nbt.getList("Professions", NbtElement.STRING_TYPE);
		for (NbtElement professionName: professions) {
			NbtString name = (NbtString)professionName;
			this.professions.add(name.asString());
		}
		NbtCompound professionsExp = nbt.getCompound("ProfessionsExp");
		for (String key: professionsExp.getKeys()) {
			this.professionsExp.put(key, professionsExp.getInt(key));
		}
	}
}
