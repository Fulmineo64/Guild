package dev.fulmineo.guild.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.Quest;
import dev.fulmineo.guild.data.QuestProfession;
import dev.fulmineo.guild.data.Quest.QuestData;
import dev.fulmineo.guild.screen.QuestsScreenHandler.ProfessionData;
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
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class QuestsScreen extends HandledScreen<QuestsScreenHandler> {
	private static final Identifier TEXTURE = new Identifier(Guild.MOD_ID, "textures/gui/container/guild.png");
	private ProfessionButton[] professions = new ProfessionButton[7];
	private List<QuestButton> available = new ArrayList<>();
	private List<QuestButton> accepted = new ArrayList<>();
	private List<Quest> professionQuests = new ArrayList<>();
	int indexStartOffset;
	private String professionName;
	private ProfessionData professionData;
	private boolean deleteMode;

   	public QuestsScreen(QuestsScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.passEvents = false;
		this.backgroundWidth = 276;
		this.backgroundHeight = 200;
		this.titleY = 4;
   	}

	protected void init() {
		super.init();
		int w = (this.width - this.backgroundWidth) / 2;
		int h = (this.height - this.backgroundHeight) / 2;
		this.addDrawableChild(new InfoButton(w + 6, h + 16, (button) -> {}));
		this.addDrawableChild(new ButtonWidget(w + this.backgroundWidth - 66, h + 16, 60, 20, new TranslatableText("button.guild.quest.delete"), (button) -> {
			this.deleteMode = !this.deleteMode;
			button.setMessage(this.deleteMode ? new TranslatableText("button.guild.quest.cancel") : new TranslatableText("button.guild.quest.delete"));
			for(int i = 0; i < this.available.size(); ++i) {
				this.available.get(i).active = this.deleteMode ? true : this.handler.acceptedQuests.size() < this.handler.maxAcceptedQuests;
			}
		}));
		int y = h + 41;
		int profNum = this.handler.professions.size();
		int x = (this.width / 2) - (((profNum * 20) + ((profNum-1) * 2)) / 2);
		for(int i = 0; i < profNum; ++i) {
			this.professions[i] = this.addDrawableChild(new ProfessionButton(x, y - 25, i, (button) -> {
				if (!button.active) return;
				this.selectProfession(((ProfessionButton)button).index);
			}));
			x += 22;
		}
		if (this.handler.professions.size() > 0) this.selectProfession(0);
	}

	private void initButtons() {
		int w = (this.width - this.backgroundWidth) / 2;
		int h = (this.height - this.backgroundHeight) / 2;
		int y = h + 52;
		for(int i = 0; i < this.handler.professions.size(); ++i) {
			this.professions[i].active = this.handler.professions.get(i).name != this.professionName;
		}
		for(int i = 0; i < this.available.size(); ++i) {
			this.remove(this.available.get(i));
		}
		for(int i = 0; i < this.accepted.size(); ++i) {
			this.remove(this.accepted.get(i));
		}

		this.available.clear();
		this.accepted.clear();

		for(int i = 0; i < this.professionQuests.size(); i++){
			this.professionQuests.get(i).updateTasksAndRewards();
			QuestsScreen.this.itemRenderer.zOffset = 0.0F;
			AvailableQuestButton btn = this.addDrawableChild(new AvailableQuestButton(w + 5, y, i, (button) -> {
				int index = ((AvailableQuestButton)button).index;
				if (this.deleteMode) {
					this.handler.deleteAvailableQuest(this.professionName, index);
				} else {
					if (!button.active || this.handler.acceptedQuests.size() == this.handler.maxAcceptedQuests) return;
					this.handler.acceptQuest(this.professionName, index);
					this.professionQuests = handler.availableQuests.get(this.professionName);
				}
				this.initButtons();
			}));
			btn.active = this.handler.acceptedQuests.size() < this.handler.maxAcceptedQuests;
			this.available.add(btn);
			y += 20;
		}
		y = h + 52;
		for(int i = 0; i < this.handler.acceptedQuests.size(); i++){
			this.handler.acceptedQuests.get(i).updateTasksAndRewards();
			AcceptedQuestButton btn = this.addDrawableChild(new AcceptedQuestButton(w + 143, y, i, (button) -> {
				int index = ((AcceptedQuestButton)button).index;
				if (this.deleteMode) {
					this.handler.deleteAcceptedQuest(index);
				} else if (this.handler.acceptedQuests.size() > index) {
					this.handler.tryCompleteQuest(index);
				}
				this.initButtons();
			}));
			this.accepted.add(btn);
			y += 20;
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

		// Draws the background
		drawTexture(matrices, this.x, this.y, this.getZOffset(), 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 512, 256);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);

		int i = (this.width - this.backgroundWidth) / 2;
		int j = (this.height - this.backgroundHeight) / 2;

		this.drawLevelInfo(matrices, i, j);
	}

	private void drawLevelInfo(MatrixStack matrices, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		drawTexture(matrices, x + 85, y + 44, this.getZOffset(), 0.0F, 216.0F, 102, 5, 512, 256);
		drawTexture(matrices, x + 85, y + 44, this.getZOffset(), 0.0F, 221.0F, this.professionData.levelPerc+ 1, 5, 512, 256);

		int tx = x + 138;
		int ty = y + 38;
		String val = String.valueOf(this.professionData.level + 1);
		tx -= this.textRenderer.getWidth(val) / 2;

		this.textRenderer.draw(matrices, val, (float)(tx + 1), (float)ty, 0);
		this.textRenderer.draw(matrices, val, (float)(tx - 1), (float)ty, 0);
		this.textRenderer.draw(matrices, val, (float)tx, (float)(ty + 1), 0);
		this.textRenderer.draw(matrices, val, (float)tx, (float)(ty - 1), 0);
		this.textRenderer.draw(matrices, val, (float)tx, (float)ty, 8453920);
	}

	private void selectProfession(int index) {
		QuestProfession profession = this.handler.professions.get(index);
		this.professionName = profession.name;
		this.professionQuests = handler.availableQuests.get(this.professionName);
		if (this.professionQuests == null) this.professionQuests = new ArrayList<>();
		this.professionData = this.handler.professionsData.get(this.professionName);
		this.initButtons();
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
				if (Guild.CONFIG.expirationTicks != 0) {
					tooltip.add(new LiteralText("⌚").formatted(Formatting.DARK_AQUA).append(" ").append(new TranslatableText("screen.guild.quests.quest_expiration")));
					tooltip.add(new TranslatableText("screen.guild.quests.quest_expiration.description", (int)(Guild.CONFIG.expirationTicks / 60 / 20)).formatted(Formatting.DARK_GRAY));
				}
				tooltip.add(new LiteralText("⌚").formatted(Formatting.GRAY).append(" ").append(new TranslatableText("screen.guild.quests.time_available")));
				tooltip.add(new TranslatableText("screen.guild.quests.time_available.description").formatted(Formatting.DARK_GRAY));
				tooltip.add(new TranslatableText("screen.guild.quests.time_available.description2").formatted(Formatting.DARK_GRAY));
				tooltip.add(new TranslatableText("screen.guild.quests.time_available.description3").formatted(Formatting.DARK_GRAY));
				tooltip.add(new LiteralText("✉").formatted(Formatting.GRAY).append(" ").append(new TranslatableText("screen.guild.quests.item")));
				tooltip.add(new TranslatableText("screen.guild.quests.item.description").formatted(Formatting.DARK_GRAY));
				tooltip.add(new TranslatableText("screen.guild.quests.item.description2").formatted(Formatting.DARK_GRAY));
				tooltip.add(new LiteralText("🗡").formatted(Formatting.GRAY).append(" ").append(new TranslatableText("screen.guild.quests.slay")));
				tooltip.add(new TranslatableText("screen.guild.quests.slay.description").formatted(Formatting.DARK_GRAY));
				tooltip.add(new LiteralText("✙").formatted(Formatting.GRAY).append(" ").append(new TranslatableText("screen.guild.quests.cure")));
				tooltip.add(new TranslatableText("screen.guild.quests.cure.description").formatted(Formatting.DARK_GRAY));
				tooltip.add(new LiteralText("✦").formatted(Formatting.GRAY).append(" ").append(new TranslatableText("screen.guild.quests.summon")));
				tooltip.add(new TranslatableText("screen.guild.quests.summon.description").formatted(Formatting.DARK_GRAY));
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
				QuestProfession profession = this.getQuestProfession();
				tooltip.add(QuestProfession.getTranslatedText(profession.name).formatted(Formatting.GOLD));
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

			int xi = this.x + 3;
			int yi = this.y + 1;

			for (QuestData data: quest.tasks) {
				QuestsScreen.this.itemRenderer.renderInGui(data.stack, xi, yi);
				QuestsScreen.this.itemRenderer.renderGuiItemOverlay(QuestsScreen.this.textRenderer, data.stack, xi - 10, yi - 9, data.icon);
				this.renderGuiItemOverlay(QuestsScreen.this.textRenderer, data.stack, xi, yi, data.needed - data.count);
				xi += 20;
			}

			xi = x + 108;
			for (QuestData data: quest.rewards) {
				QuestsScreen.this.itemRenderer.renderInGui(data.stack, xi, yi);
				this.renderGuiItemOverlay(QuestsScreen.this.textRenderer, data.stack, xi, yi, data.count);
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
				MutableText text = new TranslatableText("screen.guild.quests.tasks").formatted(Formatting.BLUE).append("      ");
				long time = QuestsScreen.this.handler.world.getTime();
				String accTime = quest.getAcceptationTime(time);
				String remTime = quest.getRemainingTime(time);
				if (accTime.length() > 0) {
					text.append(new LiteralText("⌚ "+accTime).formatted(accTime == "00:00" ? Formatting.RED : Formatting.DARK_AQUA));
				} else {
					text.append("           ");
				}
				if (remTime.length() > 0) {
					if (accTime.length() > 0) text.append("  ");
					text.append(new LiteralText("⌚ "+remTime).formatted(remTime == "00:00" ? Formatting.RED : Formatting.GRAY));
				}
				tooltip.add(text);
				for (QuestData task: quest.tasks) {
					tooltip.add(
						new LiteralText(task.icon).formatted(Formatting.GRAY)
						.append(" ")
						.append(task.stack.getName())
						.append(" ")
						.append(String.valueOf(task.count))
						.append(" / ")
						.append(String.valueOf(task.needed))
					);
				}
				tooltip.add(new TranslatableText("screen.guild.quests.rewards").formatted(Formatting.GREEN));
				for (QuestData reward: quest.rewards) {
					tooltip.add(
						new LiteralText("").formatted(Formatting.GRAY)
						.append(reward.stack.getName())
						.append(" ")
						.append(String.valueOf(reward.count))
					);
				}
				QuestsScreen.this.renderTooltip(matrices, tooltip, Optional.empty(), mouseX, mouseY);
			}
		}
	}
}
