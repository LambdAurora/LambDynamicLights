/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.gui;

import dev.lambdaurora.lambdynlights.DynamicLightsConfig;
import dev.lambdaurora.lambdynlights.ExplosiveLightingMode;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.LambDynLightsCompat;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.option.*;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the settings screen of LambDynamicLights.
 *
 * @author LambdAurora
 * @version 1.3.4
 * @since 1.0.0
 */
public class SettingsScreen extends SpruceScreen {
	private final DynamicLightsConfig config;
	private final Screen parent;
	private final SpruceOption entitiesOption;
	private final SpruceOption blockEntitiesOption;
	private final SpruceOption waterSensitiveOption;
	private final SpruceOption creeperLightingOption;
	private final SpruceOption tntLightingOption;
	private final SpruceOption resetOption;
	private SpruceOptionListWidget list;

	public SettingsScreen(@Nullable Screen parent) {
		super(new TranslatableText("lambdynlights.menu.title"));
		this.parent = parent;
		this.config = LambDynLights.get().config;

		this.entitiesOption = new SpruceBooleanOption("lambdynlights.option.entities",
				this.config::hasEntitiesLightSource,
				this.config::setEntitiesLightSource,
				new TranslatableText("lambdynlights.tooltip.entities"), true);
		this.blockEntitiesOption = new SpruceBooleanOption("lambdynlights.option.block_entities",
				this.config::hasBlockEntitiesLightSource,
				this.config::setBlockEntitiesLightSource,
				new TranslatableText("lambdynlights.tooltip.block_entities"), true);
		this.waterSensitiveOption = new SpruceBooleanOption("lambdynlights.option.water_sensitive",
				this.config::hasWaterSensitiveCheck,
				this.config::setWaterSensitiveCheck,
				new TranslatableText("lambdynlights.tooltip.water_sensitive"), true);
		this.creeperLightingOption = new SpruceCyclingOption("entity.minecraft.creeper",
				amount -> this.config.setCreeperLightingMode(this.config.getCreeperLightingMode().next()),
				option -> option.getDisplayText(this.config.getCreeperLightingMode().getTranslatedText()),
				new TranslatableText("lambdynlights.tooltip.creeper_lighting",
						ExplosiveLightingMode.OFF.getTranslatedText(),
						ExplosiveLightingMode.SIMPLE.getTranslatedText(),
						ExplosiveLightingMode.FANCY.getTranslatedText()));
		this.tntLightingOption = new SpruceCyclingOption("block.minecraft.tnt",
				amount -> this.config.setTntLightingMode(this.config.getTntLightingMode().next()),
				option -> option.getDisplayText(this.config.getTntLightingMode().getTranslatedText()),
				new TranslatableText("lambdynlights.tooltip.tnt_lighting",
						ExplosiveLightingMode.OFF.getTranslatedText(),
						ExplosiveLightingMode.SIMPLE.getTranslatedText(),
						ExplosiveLightingMode.FANCY.getTranslatedText()));
		this.resetOption = SpruceSimpleActionOption.reset(btn -> {
			this.config.reset();
			MinecraftClient client = MinecraftClient.getInstance();
			this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
		});
	}

	@Override
	public void removed() {
		super.removed();
		this.config.save();
	}

	private int getTextHeight() {
		return (5 + this.textRenderer.fontHeight) * 3 + 5;
	}

	@Override
	protected void init() {
		super.init();

		this.list = new SpruceOptionListWidget(Position.of(this, 0, 43), this.width, this.height - 43 - 29 - this.getTextHeight());
		this.list.addSingleOptionEntry(this.config.dynamicLightsModeOption);
		this.list.addSingleOptionEntry(new SpruceSeparatorOption("lambdynlights.menu.light_sources", true, null));
		this.list.addOptionEntry(this.entitiesOption, this.blockEntitiesOption);
		this.list.addOptionEntry(this.waterSensitiveOption, null);
		this.list.addOptionEntry(this.creeperLightingOption, this.tntLightingOption);
		this.addDrawableChild(list);

		this.addDrawableChild(this.resetOption.createWidget(Position.of(this, this.width / 2 - 155, this.height - 29), 150));
		this.addDrawableChild(new SpruceButtonWidget(Position.of(this, this.width / 2 - 155 + 160, this.height - 29), 150, 20,
				SpruceTexts.GUI_DONE,
				btn -> this.client.setScreen(this.parent)));
	}

	@Override
	public void renderBackground(MatrixStack matrices) {
		this.renderBackgroundTexture(0);
	}

	@Override
	public void renderTitle(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 18, 16777215);
		if (LambDynLightsCompat.isCanvasInstalled()) {
			drawCenteredText(matrices, this.textRenderer, new TranslatableText("lambdynlights.menu.canvas.1"),
					this.width / 2, this.height - 29 - (5 + this.textRenderer.fontHeight) * 3, 0xffff0000);
			drawCenteredText(matrices, this.textRenderer, new TranslatableText("lambdynlights.menu.canvas.2"),
					this.width / 2, this.height - 29 - (5 + this.textRenderer.fontHeight) * 2, 0xffff0000);
		}
	}
}
