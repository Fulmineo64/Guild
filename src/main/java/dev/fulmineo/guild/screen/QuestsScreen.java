package dev.fulmineo.guild.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.DataManager;
import dev.fulmineo.guild.data.Quest;
import dev.fulmineo.guild.data.QuestHelper;
import dev.fulmineo.guild.data.QuestLevel;
import dev.fulmineo.guild.data.QuestProfession;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public class QuestsScreen extends HandledScreen<QuestsScreenHandler> {
	private static final Identifier TEXTURE = new Identifier(Guild.MOD_ID, "textures/gui/container/guild.png");
	private ProfessionButton[] professions = new ProfessionButton[7];
	private QuestButton[] available = new QuestButton[7];
	private QuestButton[] accepted = new QuestButton[7];
	int indexStartOffset;
	private String professionName;
	private List<Quest> professionQuests;
	private List<QuestLevel> professionLevels;
	private int professionLevel;
	private boolean maxLevelReached;
	private int professionLevelPerc;
	private boolean deleteMode;
	private PlayerInventory inventory;

   	public QuestsScreen(QuestsScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.inventory = inventory;
		this.passEvents = false;
		this.backgroundWidth = 276;
		this.backgroundHeight = 200;
		this.titleY = 4;
		if (this.professionQuests == null) {
			this.professionQuests = new ArrayList<>();
		}
   	}

	protected void init() {
		super.init();
		if (this.handler.professions.size() > 0) this.selectProfession(0);
		int w = (this.width - this.backgroundWidth) / 2;
		int h = (this.height - this.backgroundHeight) / 2;
		/*this.addDrawableChild(new ButtonWidget(w + 4, h + 27,  60, 20, new TranslatableText("button.quest.refresh"), (button) -> {
			ClientNetworkManager.openGuildScreen();
		}));*/
		this.addDrawableChild(new InfoButton(w + 6, h + 16, (button) -> {}));
		this.addDrawableChild(new ButtonWidget(w + this.backgroundWidth - 66, h + 16, 60, 20, new TranslatableText("button.quest.delete"), (button) -> {
			this.deleteMode = !this.deleteMode;
			button.setMessage(this.deleteMode ? new TranslatableText("button.quest.cancel") : new TranslatableText("button.quest.delete"));
			for(int i = 0; i < 7; ++i) {
				this.available[i].active = this.deleteMode ? true : this.handler.acceptedQuests.size() < 7;
			}
		}));
		this.initButtons();
	}

	private void initButtons() {
		this.updateItemCompletion();
		int w = (this.width - this.backgroundWidth) / 2;
		int h = (this.height - this.backgroundHeight) / 2;
		int y = h + 41;
		int profNum = this.handler.professions.size();
		int x = (this.width / 2) - (((profNum * 20) + ((profNum-1) * 2)) / 2);
		for(int i = 0; i < profNum; ++i) {
			this.professions[i] = this.addDrawableChild(new ProfessionButton(x, y - 25, i, (button) -> {
				if (!button.active) return;
				this.selectProfession(((ProfessionButton)button).index);
				this.initButtons();
			}));
			this.professions[i].active = this.handler.professions.get(i).name != this.professionName;
			x += 22;
		}
		y = h + 52;
		for(int i = 0; i < 7; ++i) {
			this.available[i] = this.addDrawableChild(new AvailableQuestButton(w + 5, y, i, (button) -> {
				if (this.deleteMode) {
					this.handler.deleteAvailableQuest(this.professionName, ((AvailableQuestButton)button).index);
				} else {
					if (!button.active) return;
					this.handler.acceptQuest(this.professionName, ((AvailableQuestButton)button).index);
					this.professionQuests = handler.availableQuests.get(this.professionName);
					this.initButtons();
				}
			}));
			this.available[i].active = this.handler.acceptedQuests.size() < 7;
			y += 20;
		}
		y = h + 52;
		for(int i = 0; i < this.handler.acceptedQuests.size(); i++){
			this.accepted[i] = this.addDrawableChild(new AcceptedQuestButton(w + 143, y, i, (button) -> {
				if (this.deleteMode) {
					this.handler.deleteAcceptedQuest(((AcceptedQuestButton)button).index);
				} else {
					this.handler.tryCompleteQuest(((AcceptedQuestButton)button).index);
				}
				this.initButtons();
			}));
			y += 20;
		}
	}

	private void updateItemCompletion() {
		Map<String, Integer> map = new HashMap<>();

		ImmutableList<DefaultedList<ItemStack>> mainAndOffhand = ImmutableList.of(inventory.main, inventory.offHand);
		Iterator<DefaultedList<ItemStack>> iterator = mainAndOffhand.iterator();
		while (iterator.hasNext()) {
			DefaultedList<ItemStack> defaultedList = (DefaultedList<ItemStack>) iterator.next();
			for (int i = 0; i < defaultedList.size(); ++i) {
				ItemStack stack = defaultedList.get(i);
				if (!stack.isOf(Items.AIR)) {
					String id = Registry.ITEM.getId(stack.getItem()).toString();
					Integer val = map.get(id);
					map.put(id, (val == null ? 0 : val) + stack.getCount());
				}
			}
		}

		for (Quest quest: this.handler.acceptedQuests) {
			NbtList items = quest.getItems();
			for (NbtElement elm: items) {
				NbtCompound entry = (NbtCompound)elm;
				Integer val = map.get(entry.getString("Name"));
				entry.putInt("Count", Math.min(val == null ? 0 : val, entry.getInt("Needed")));
			}
		}
	}

	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		float x = (float)((70 - this.textRenderer.getWidth(this.title)) / 2);
		this.textRenderer.draw(matrices, this.title, x, (float)this.titleY, 4210752);

		this.textRenderer.draw(matrices, new TranslatableText("screen.guild.quests.available"), 6, 40, 4210752);
		MutableText accepted = new TranslatableText("screen.guild.quests.accepted");
		this.textRenderer.draw(matrices, accepted, this.backgroundWidth - 6 - this.textRenderer.getWidth(accepted), 40, 4210752);
	}

	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int i = (this.width - this.backgroundWidth) / 2;
		int j = (this.height - this.backgroundHeight) / 2;

		// Draws the background
		drawTexture(matrices, i, j, this.getZOffset(), 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 512);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);

		int i = (this.width - this.backgroundWidth) / 2;
		int j = (this.height - this.backgroundHeight) / 2;

		// this.renderScrollbar(matrices, i, j, this.available.length);
		// this.renderScrollbar(matrices, i, j, this.accepted.length);

		this.drawLevelInfo(matrices, i, j);
	}

	private void drawLevelInfo(MatrixStack matrices, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		drawTexture(matrices, x + 85, y + 44, this.getZOffset(), 0.0F, 216.0F, 102, 5, 256, 512);
		if (!this.maxLevelReached) {
			drawTexture(matrices, x + 85, y + 44, this.getZOffset(), 0.0F, 221.0F, this.professionLevelPerc + 1, 5, 256, 512);
		}

		int tx = x + 135;
		int ty = y + 38;
		String val = String.valueOf(this.professionLevel + 1);

		this.textRenderer.draw(matrices, val, (float)(tx + 1), (float)ty, 0);
		this.textRenderer.draw(matrices, val, (float)(tx - 1), (float)ty, 0);
		this.textRenderer.draw(matrices, val, (float)tx, (float)(ty + 1), 0);
		this.textRenderer.draw(matrices, val, (float)tx, (float)(ty - 1), 0);
		this.textRenderer.draw(matrices, val, (float)tx, (float)ty, 8453920);
	}

	/*private void renderScrollbar(MatrixStack matrices, int x, int y, int size) {
		int i = size + 1 - 7;
		if (i > 1) {
			int j = 139 - (27 + (i - 1) * 139 / i);
			int k = 1 + j / i + 139 / i;
			int m = Math.min(113, this.indexStartOffset * k);
			if (this.indexStartOffset == i - 1) {
				m = 113;
			}

		   	drawTexture(matrices, x + 94, y + 18 + m, this.getZOffset(), 0.0F, 199.0F, 6, 27, 256, 512);
		} else {
		   	drawTexture(matrices, x + 94, y + 18, this.getZOffset(), 6.0F, 199.0F, 6, 27, 256, 512);
		}
	}*/

	private void selectProfession(int index) {
		QuestProfession profession = this.handler.professions.get(index);
		this.professionName = profession.name;
		this.professionLevels = DataManager.levels.get(profession.levelsPool);
		this.professionQuests = handler.availableQuests.get(this.professionName);
		if (this.professionQuests == null) this.professionQuests = new ArrayList<>();
		int exp = this.handler.professionsExp.get(this.professionName);
		this.professionLevel = QuestHelper.getCurrentLevel(this.professionLevels, exp);
		this.maxLevelReached = this.professionLevel == this.professionLevels.size()-1;
		QuestLevel currentLevel = this.professionLevels.get(this.professionLevel);
		QuestLevel nextLevel = this.professionLevels.get(this.professionLevel+1);
		this.professionLevelPerc = (int)(((float)(exp - currentLevel.exp) / (float)(nextLevel.exp - currentLevel.exp)) * 100);
	}

	@Environment(EnvType.CLIENT)
	class InfoButton extends ButtonWidget {
		public InfoButton(int x, int y, ButtonWidget.PressAction onPress) {
			super(x, y, 20, 20, new LiteralText("?"), onPress);
		}

		public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
			if (this.hovered) {
				List<Text> tooltip = new ArrayList<>();
				tooltip.add(new TranslatableText("screen.guild.quests.legend").formatted(Formatting.AQUA));
				tooltip.add(new LiteralText("⌚").formatted(Formatting.GRAY).append(" ").append(new TranslatableText("screen.guild.quests.time_available")));
				tooltip.add(new TranslatableText("screen.guild.quests.time_available.description").formatted(Formatting.DARK_GRAY));
				tooltip.add(new TranslatableText("screen.guild.quests.time_available.description2").formatted(Formatting.DARK_GRAY));
				tooltip.add(new TranslatableText("screen.guild.quests.time_available.description3").formatted(Formatting.DARK_GRAY));
				tooltip.add(new LiteralText("✉").formatted(Formatting.GRAY).append(" ").append(new TranslatableText("screen.guild.quests.deliver_item")));
				tooltip.add(new TranslatableText("screen.guild.quests.deliver_item.description").formatted(Formatting.DARK_GRAY));
				tooltip.add(new TranslatableText("screen.guild.quests.deliver_item.description2").formatted(Formatting.DARK_GRAY));
				tooltip.add(new LiteralText("🗡").formatted(Formatting.GRAY).append(" ").append(new TranslatableText("screen.guild.quests.slay")));
				tooltip.add(new TranslatableText("screen.guild.quests.slay.description").formatted(Formatting.DARK_GRAY));
				/*tooltip.add(new LiteralText("⚒").formatted(Formatting.GRAY).append(" ").append(new TranslatableText("screen.guild.quests.build")));
				tooltip.add(new TranslatableText("screen.guild.quests.build.description").formatted(Formatting.DARK_GRAY));*/
				QuestsScreen.this.renderTooltip(matrices, tooltip, Optional.empty(), mouseX, mouseY);
			}
		}
	}


	@Environment(EnvType.CLIENT)
	class ProfessionButton extends ButtonWidget {
		final int index;
		private Item item;
		public ProfessionButton(int x, int y, int index, ButtonWidget.PressAction onPress) {
			super(x, y, 20, 20, LiteralText.EMPTY, onPress);
			this.index = index;
		}

		public QuestProfession getQuestProfession() {
			return QuestsScreen.this.handler.professions.get(this.index);
		}

		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			QuestProfession profession = this.getQuestProfession();
			if (profession == null) return;
			super.renderButton(matrices, mouseX, mouseY, delta);
			QuestsScreen.this.itemRenderer.zOffset = 100.0F;
			if (this.item == null) {
				this.item = Registry.ITEM.get(new Identifier(profession.icon));
			}
			ItemStack stack = new ItemStack(this.item);
			QuestsScreen.this.itemRenderer.renderInGui(stack, this.x + 2, this.y + 2);
		}

		public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
			if (this.hovered) {
				List<Text> tooltip = new ArrayList<>();
				tooltip.add(new TranslatableText("profession."+this.getQuestProfession().name.replace(":", ".")).formatted(Formatting.GOLD));
				QuestsScreen.this.renderTooltip(matrices, tooltip, Optional.empty(), mouseX, mouseY);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class AvailableQuestButton extends QuestButton {
		public AvailableQuestButton(int x, int y, int index, ButtonWidget.PressAction onPress) {
			super(x, y, index, onPress);
		}

		public Quest getQuest() {
			return QuestsScreen.this.professionQuests.size() > this.index ? QuestsScreen.this.professionQuests.get(this.index) : null;
		}
	}

	@Environment(EnvType.CLIENT)
	class AcceptedQuestButton extends QuestButton {
		public AcceptedQuestButton(int x, int y, int index, ButtonWidget.PressAction onPress) {
			super(x, y, index, onPress);
		}

		public Quest getQuest() {
			return QuestsScreen.this.handler.acceptedQuests.size() > this.index ? QuestsScreen.this.handler.acceptedQuests.get(this.index) : null;
		}

		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			Quest quest = this.getQuest();
			if (quest == null) return;
			super.renderButton(matrices, mouseX, mouseY, delta);
		}
	}

	@Environment(EnvType.CLIENT)
	abstract class QuestButton extends ButtonWidget {
		final int index;

		public QuestButton(int x, int y, int index, ButtonWidget.PressAction onPress) {
			super(x, y, 126, 20, LiteralText.EMPTY, onPress);
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}

		public Quest getQuest() {
			return null;
		}

		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			Quest quest = this.getQuest();
			if (quest == null) return;
			super.renderButton(matrices, mouseX, mouseY, delta);
			QuestsScreen.this.itemRenderer.zOffset = 100.0F;

			int xi = this.x + 1;
			int yi = this.y + 1;

			// TODO: Remove Registry calls from the render cycle!
			NbtList items = quest.getItems();
			for (NbtElement elm: items) {
				NbtCompound entry = (NbtCompound)elm;
				Item item = Registry.ITEM.get(new Identifier(entry.getString("Name")));
				ItemStack stack = new ItemStack(item);
				QuestsScreen.this.itemRenderer.renderInGui(stack, xi, yi);
				this.renderGuiItemOverlay(QuestsScreen.this.textRenderer, stack, xi, yi, entry.getInt("Needed") - entry.getInt("Count"));
				xi += 18;
			}

			NbtList entities = quest.getEntities();
			for (NbtElement elm: entities) {
				NbtCompound entry = (NbtCompound)elm;
				Item spawnEgg = Registry.ITEM.get(new Identifier(entry.getString("Name")+"_spawn_egg"));
				if (spawnEgg == null) {
					spawnEgg = Items.DIAMOND_SWORD;
				}
				ItemStack stack = new ItemStack(spawnEgg);
				QuestsScreen.this.itemRenderer.renderInGui(stack, xi, yi);
				this.renderGuiItemOverlay(QuestsScreen.this.textRenderer, stack, xi, yi, entry.getInt("Needed") - entry.getInt("Count"));
				xi += 18;
			}

			xi = x + 108;
			NbtList rewards = quest.getRewards();
			for (NbtElement elm: rewards) {
				NbtCompound entry = (NbtCompound)elm;
				Item item = Registry.ITEM.get(new Identifier(entry.getString("Name")));
				ItemStack stack = new ItemStack(item);
				QuestsScreen.this.itemRenderer.renderInGui(stack, xi, yi);
				this.renderGuiItemOverlay(QuestsScreen.this.textRenderer, stack, xi, yi, entry.getInt("Count"));
				xi -= 18;
			}

			QuestsScreen.this.itemRenderer.zOffset = 0.0F;
		}

		protected void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, int val) {
			QuestsScreen.this.itemRenderer.renderGuiItemOverlay(QuestsScreen.this.textRenderer, stack, x, y, val > 0 ? String.valueOf(val) : "✔");
		}

		public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
			if (this.hovered) {
				Quest quest = this.getQuest();
				if (quest == null) return;
				List<Text> tooltip = new ArrayList<>();
				MutableText text = new TranslatableText("item.guild.quest_scroll.tasks").formatted(Formatting.BLUE);
				String time = quest.getRemainingTime(QuestsScreen.this.handler.world.getTime());
				if (time.length() > 0) {
					text.append("               ").append(new LiteralText("⌚ "+time).formatted(time == "00:00" ? Formatting.RED : Formatting.GRAY));
				}
				tooltip.add(text);
				// TODO: Remove Registry calls from the render cycle!
				NbtList items = quest.getItems();
				for (NbtElement elem : items) {
					NbtCompound entry = (NbtCompound)elem;
					tooltip.add(
						new LiteralText("✉").formatted(Formatting.GRAY)
						.append(" ")
						.append(new TranslatableText(Registry.ITEM.get(new Identifier(entry.getString("Name"))).getTranslationKey()))
						.append(" ")
						.append(String.valueOf(entry.getInt("Count")))
						.append(" / ")
						.append(String.valueOf(entry.getInt("Needed")))
					);
				}
				NbtList entities = quest.getEntities();
				for (NbtElement elem : entities) {
					NbtCompound entry = (NbtCompound)elem;
					tooltip.add(
						new LiteralText("🗡").formatted(Formatting.GRAY)
						.append(" ")
						.append(new TranslatableText(Registry.ENTITY_TYPE.get(new Identifier(entry.getString("Name"))).getTranslationKey()))
						.append(" ")
						.append(String.valueOf(entry.getInt("Count")))
						.append(" / ")
						.append(String.valueOf(entry.getInt("Needed")))
					);
				}
				tooltip.add(new TranslatableText("item.guild.quest_scroll.rewards").formatted(Formatting.GREEN));
				NbtList rewards = quest.getRewards();
				for (NbtElement elem : rewards) {
					NbtCompound entry = (NbtCompound)elem;
					tooltip.add(
						new TranslatableText(Registry.ITEM.get(new Identifier(entry.getString("Name"))).getTranslationKey()).formatted(Formatting.GRAY)
						.append(" ")
						.append(String.valueOf(entry.getInt("Count")))
					);
				}
				QuestsScreen.this.renderTooltip(matrices, tooltip, Optional.empty(), mouseX, mouseY);
			}
		}
	}
}
