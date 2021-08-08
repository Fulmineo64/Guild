package dev.fulmineo.guild.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.fulmineo.guild.Guild;
import dev.fulmineo.guild.data.Quest;
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
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GuildScreen extends HandledScreen<GuildScreenHandler> {
	private static final Identifier TEXTURE = new Identifier(Guild.MOD_ID, "textures/gui/container/guild.png");
	private ProfessionButton[] professions = new ProfessionButton[7];
	private QuestButton[] available = new QuestButton[7];
	private QuestButton[] accepted = new QuestButton[7];
	int indexStartOffset;
	private List<Quest> professionQuests;
	private String selectedProfession;

   	public GuildScreen(GuildScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.passEvents = false;
		this.backgroundWidth = 276;
		this.selectedProfession = handler.professions.get(0).name;
		this.professionQuests = handler.availableQuests.get(this.selectedProfession);
		if (this.professionQuests == null) {
			this.professionQuests = new ArrayList<>();
		}
   	}

	protected void init() {
		super.init();
		this.initButtons();
	}

	private void initButtons() {
		int w = (this.width - this.backgroundWidth) / 2;
		int h = (this.height - this.backgroundHeight) / 2;
		int y = h + 16 + 2;
		int profNum = this.handler.professions.size();
		int x = (this.width / 2) - (((profNum * 20) - ((profNum-1) * 2)) / 2);
		for(int i = 0; i < profNum; ++i) {
			this.professions[i] = this.addDrawableChild(new ProfessionButton(x, y - 20, i, (button) -> {
				this.selectedProfession = this.handler.professions.get(((ProfessionButton)button).index).name;
				this.professionQuests = handler.availableQuests.get(this.selectedProfession);
				this.initButtons();
			}));
			x += 22;
		}
		for(int i = 0; i < 7; ++i) {
			this.available[i] = this.addDrawableChild(new AvailableQuestButton(w + 5, y, i, (button) -> {
				this.handler.acceptQuest(this.selectedProfession, ((AvailableQuestButton)button).index);
				this.professionQuests = handler.availableQuests.get(this.selectedProfession);
				this.initButtons();
			}));
			y += 20;
		}
		y = h + 16 + 2;
		for(int i = 0; i < this.handler.acceptedQuests.size(); i++){
			this.accepted[i] = this.addDrawableChild(new AcceptedQuestButton(w + 174, y, i, (button) -> {
				this.handler.tryCompleteQuest(((AcceptedQuestButton)button).index);
				this.initButtons();
			}));
			y += 20;
		}
	}

	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 4210752);
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
		for(int i = 0; i < this.handler.professions.size(); ++i) {
			ProfessionButton professionButton = this.professions[i];
			if (professionButton != null) {
				if (professionButton.isHovered()) {
					professionButton.renderTooltip(matrices, mouseX, mouseY);
				}

				professionButton.visible = professionButton.index < 7;
			}
		}

		for(int i = 0; i < Math.min(this.professionQuests.size(), 7); ++i) {
			QuestButton questButton = this.available[i];
			if (questButton != null) {
				if (questButton.isHovered()) {
					questButton.renderTooltip(matrices, mouseX, mouseY);
				}

				questButton.visible = questButton.index < 7;
			}
		}

		for(int i = 0; i < this.handler.acceptedQuests.size(); ++i) {
			QuestButton questButton = this.accepted[i];
			if (questButton.isHovered()) {
				questButton.renderTooltip(matrices, mouseX, mouseY);
			}

			questButton.visible = questButton.index < 7;
		}

		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);

		int i = (this.width - this.backgroundWidth) / 2;
		int j = (this.height - this.backgroundHeight) / 2;

		this.renderScrollbar(matrices, i, j, this.available.length);
		// this.renderScrollbar(matrices, i, j, this.accepted.length);
	}

	private void renderScrollbar(MatrixStack matrices, int x, int y, int size) {
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
	}

	@Environment(EnvType.CLIENT)
	class ProfessionButton extends ButtonWidget {
		final int index;
		public ProfessionButton(int x, int y, int index, ButtonWidget.PressAction onPress) {
			super(x, y, 20, 20, LiteralText.EMPTY, onPress);
			this.index = index;
			this.visible = false;
		}

		public QuestProfession getQuestProfession() {
			return GuildScreen.this.handler.professions.get(this.index);
		}

		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			QuestProfession profession = this.getQuestProfession();
			if (profession == null) return;
			super.renderButton(matrices, mouseX, mouseY, delta);
			GuildScreen.this.itemRenderer.zOffset = 100.0F;

			// TODO: Remove Registry calls from the render cycle!
			Item item = Registry.ITEM.get(new Identifier(profession.icon));
			ItemStack stack = new ItemStack(item);
			GuildScreen.this.itemRenderer.renderInGui(stack, this.x + 2, this.y + 2);
		}

		/*public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
			if (this.hovered) {
				List<Text> tooltip = new ArrayList<>();
				QuestProfession profession = this.getQuestProfession();
				tooltip.add(profession.name);
				GuildScreen.this.renderTooltip(matrices, tooltip, Optional.empty(), mouseX, mouseY);
			}
		}*/
	}

	@Environment(EnvType.CLIENT)
	class AvailableQuestButton extends QuestButton {
		public AvailableQuestButton(int x, int y, int index, ButtonWidget.PressAction onPress) {
			super(x, y, index, onPress);
		}

		public Quest getQuest() {
			return GuildScreen.this.professionQuests.size() > this.index ? GuildScreen.this.professionQuests.get(this.index) : null;
		}
	}

	@Environment(EnvType.CLIENT)
	class AcceptedQuestButton extends QuestButton {
		public AcceptedQuestButton(int x, int y, int index, ButtonWidget.PressAction onPress) {
			super(x, y, index, onPress);
		}

		public Quest getQuest() {
			return GuildScreen.this.handler.acceptedQuests.size() > this.index ? GuildScreen.this.handler.acceptedQuests.get(this.index) : null;
		}
	}


	@Environment(EnvType.CLIENT)
	abstract class QuestButton extends ButtonWidget {
		final int index;

		public QuestButton(int x, int y, int index, ButtonWidget.PressAction onPress) {
			super(x, y, 89, 20, LiteralText.EMPTY, onPress);
			this.index = index;
			this.visible = false;
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
			GuildScreen.this.itemRenderer.zOffset = 100.0F;

			int xi = this.x;
			int yi = this.y + 1;

			// TODO: Remove Registry calls from the render cycle!
			NbtList items = quest.getItems();
			for (NbtElement elm: items) {
				NbtCompound entity = (NbtCompound)elm;
				Item item = Registry.ITEM.get(new Identifier(entity.getString("Name")));
				ItemStack stack = new ItemStack(item);
				GuildScreen.this.itemRenderer.renderInGui(stack, xi, yi);
				this.renderGuiItemOverlay(GuildScreen.this.textRenderer, stack, xi, yi, entity.getInt("Needed"));
				xi += 18;
			}

			NbtList entities = quest.getEntities();
			for (NbtElement elm: entities) {
				NbtCompound entity = (NbtCompound)elm;
				Item spawnEgg = Registry.ITEM.get(new Identifier(entity.getString("Name")+"_spawn_egg"));
				if (spawnEgg == null) {
					spawnEgg = Items.DIAMOND_SWORD;
				}
				ItemStack stack = new ItemStack(spawnEgg);
				GuildScreen.this.itemRenderer.renderInGui(stack, xi, yi);
				this.renderGuiItemOverlay(GuildScreen.this.textRenderer, stack, xi, yi, entity.getInt("Needed") - entity.getInt("Count"));
				xi += 18;
			}

			xi = x + 4 + 65;
			NbtList rewards = quest.getRewards();
			for (NbtElement elm: rewards) {
				NbtCompound reward = (NbtCompound)elm;
				Item item = Registry.ITEM.get(new Identifier(reward.getString("Name")));
				ItemStack stack = new ItemStack(item);
				GuildScreen.this.itemRenderer.renderInGui(stack, xi, yi);
				this.renderGuiItemOverlay(GuildScreen.this.textRenderer, stack, xi, yi, reward.getInt("Count"));
				xi -= 18;
			}

			GuildScreen.this.itemRenderer.zOffset = 0.0F;
		}

		protected void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, int val) {
			GuildScreen.this.itemRenderer.renderGuiItemOverlay(GuildScreen.this.textRenderer, stack, x, y, val > 0 ? String.valueOf(val) : "✔");
		}

		public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
			if (this.hovered) {
				List<Text> tooltip = new ArrayList<>();
				tooltip.add(new TranslatableText("item.guild.quest_scroll.tasks").formatted(Formatting.BLUE));
				// TODO: Remove Registry calls from the render cycle!
				NbtCompound tag = this.getQuest().getNbt();
				if (tag.contains("Items")) {
					NbtList bounties = tag.getList("Items", NbtElement.COMPOUND_TYPE);
					for (NbtElement elem : bounties) {
						NbtCompound entry = (NbtCompound)elem;
						tooltip.add(
							new TranslatableText(Registry.ITEM.get(new Identifier(entry.getString("Name"))).getTranslationKey()).formatted(Formatting.GRAY)
							.append(" ")
							.append(String.valueOf(entry.getInt("Count")))
							.append(" / ")
							.append(String.valueOf(entry.getInt("Needed")))
						);
					}
				}
				if (tag.contains("Entities")) {
					NbtList bounties = tag.getList("Entities", NbtElement.COMPOUND_TYPE);
					for (NbtElement elem : bounties) {
						NbtCompound entry = (NbtCompound)elem;
						tooltip.add(
							new TranslatableText(Registry.ENTITY_TYPE.get(new Identifier(entry.getString("Name"))).getTranslationKey()).formatted(Formatting.GRAY)
							.append(" ")
							.append(String.valueOf(entry.getInt("Count")))
							.append(" / ")
							.append(String.valueOf(entry.getInt("Needed")))
						);
					}
				}
				if (tag.contains("Rewards")) {
					tooltip.add(new TranslatableText("item.guild.quest_scroll.rewards").formatted(Formatting.GREEN));
					NbtList bounties = tag.getList("Rewards", NbtElement.COMPOUND_TYPE);
					for (NbtElement elem : bounties) {
						NbtCompound entry = (NbtCompound)elem;
						tooltip.add(
							new TranslatableText(Registry.ITEM.get(new Identifier(entry.getString("Name"))).getTranslationKey()).formatted(Formatting.GRAY)
							.append(" ")
							.append(String.valueOf(entry.getInt("Count")))
						);
					}
				}
				GuildScreen.this.renderTooltip(matrices, tooltip, Optional.empty(), mouseX, mouseY);
			}
		}
	}
}
