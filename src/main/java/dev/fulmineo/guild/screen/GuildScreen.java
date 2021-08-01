package dev.fulmineo.guild.screen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.fulmineo.guild.data.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GuildScreen extends HandledScreen<GuildScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("textures/gui/container/villager2.png");
	private WidgetButtonPage[] offers = new WidgetButtonPage[7];
	int indexStartOffset;

   	public GuildScreen(GuildScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.passEvents = false;
		this.backgroundWidth = 276;
		this.playerInventoryTitleX = 107;
   	}

	protected void init() {
		super.init();
		int i = (this.width - this.backgroundWidth) / 2;
		int j = (this.height - this.backgroundHeight) / 2;
		int k = j + 16 + 2;
		for(int l = 0; l < 7; ++l) {
			this.offers[l] = (WidgetButtonPage)this.addDrawableChild(new WidgetButtonPage(i + 5, k, l, (button) -> {}));
			k += 20;
		}
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
		WidgetButtonPage[] var19 = this.offers;
		int var20 = var19.length;

		for(int var21 = 0; var21 < var20; ++var21) {
			WidgetButtonPage widgetButtonPage = var19[var21];
			if (widgetButtonPage.isHovered()) {
				widgetButtonPage.renderTooltip(matrices, mouseX, mouseY);
			}

			widgetButtonPage.visible = widgetButtonPage.index < 7;
		}

		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);

		Map<String, List<Quest>> availableQuests = this.handler.availableQuests;
		// TODO: Get the quests of the selected profession
		List<Quest> professionQuests = availableQuests.get("guild:guard");
		if (professionQuests != null && professionQuests.size() > 0) {
			Iterator<Quest> iterator = professionQuests.iterator();

			int i = (this.width - this.backgroundWidth) / 2;
			int j = (this.height - this.backgroundHeight) / 2;
			int k = j + 16 + 1;

			while (iterator.hasNext()) {
				int n = k + 2;
				this.itemRenderer.zOffset = 100.0F;
				this.itemRenderer.renderInGui(new ItemStack(Items.NETHERITE_SWORD), i + 5 + 35, n);
				this.itemRenderer.zOffset = 0.0F;
			}
		}

		this.drawMouseoverTooltip(matrices, mouseX, mouseY);

		int i = (this.width - this.backgroundWidth) / 2;
		int j = (this.height - this.backgroundHeight) / 2;

		this.renderScrollbar(matrices, i, j, var20);
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
	class WidgetButtonPage extends ButtonWidget {
	   final int index;

		public WidgetButtonPage(int x, int y, int index, ButtonWidget.PressAction onPress) {
			super(x, y, 89, 20, LiteralText.EMPTY, onPress);
			this.index = index;
			this.visible = false;
		}

		public int getIndex() {
			return this.index;
		}


		public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
			if (this.hovered) {
				List<Text> tooltip = new ArrayList<>();
				tooltip.add(new LiteralText("ciao"));
				GuildScreen.this.renderTooltip(matrices, tooltip, Optional.empty(), mouseX, mouseY);
			}
		}
	}
}
