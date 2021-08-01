package dev.fulmineo.guild.mixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.DataManager;
import dev.fulmineo.guild.data.GuildServerPlayerEntity;
import dev.fulmineo.guild.data.Quest;
import dev.fulmineo.guild.data.QuestHelper;
import dev.fulmineo.guild.data.QuestProfession;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements GuildServerPlayerEntity {

	protected List<Quest> acceptedQuests = new ArrayList<>();
	protected Map<String, List<Quest>> availableQuests = new HashMap<>();
	private long lastQuestGenTime;

	public ServerPlayerEntityMixin(MinecraftServer server, ServerWorld world, GameProfile profile) {
		super(world, world.getSpawnPos(), world.getSpawnAngle(), profile);
	}

	// TODO: Check if you can extend non implemented functions through inject
	public void onKilledOther(ServerWorld world, LivingEntity killedEntity) {
		super.onKilledOther(world, killedEntity);
		QuestHelper.updateQuestEntities(this, killedEntity);
	}

	public List<Quest> getAcceptedQuests() {
		return this.acceptedQuests;
	}

	public void acceptQuest(Quest quest) {
		this.acceptedQuests.add(quest);
		for (Quest q: this.acceptedQuests){
			Guild.info(q.toString());
		}
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

	public List<QuestProfession> getQuestProfessions() {
		// TODO: Handle the professions correctly
		List<QuestProfession> professions = new ArrayList<>();
		professions.add(DataManager.professions.get("guild:guard"));
		return professions;
	}

	@Inject(at = @At("TAIL"), method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo info) {
		QuestHelper.writeNbt(nbt, this.acceptedQuests);
	}

	@Inject(at = @At("TAIL"), method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo info) {
		this.acceptedQuests = QuestHelper.fromNbt(nbt);
	}
}
