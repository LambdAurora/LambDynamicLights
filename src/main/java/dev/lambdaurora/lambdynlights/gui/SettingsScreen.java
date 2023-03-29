/*
 * Copyright © 2020-2022 LambdAurora <email@lambdaurora.dev>
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
import dev.lambdaurora.lambdynlights.accessor.DynamicLightHandlerHolder;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.background.Background;
import dev.lambdaurora.spruceui.background.DirtTexturedBackground;
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.option.SpruceSeparatorOption;
import dev.lambdaurora.spruceui.option.SpruceSimpleActionOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.util.RenderUtil;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the settings screen of LambDynamicLights.
 *
 * @author LambdAurora
 * @version 2.2.0
 * @since 1.0.0
 */
public class SettingsScreen extends SpruceScreen {
	private static final Background INNER_BACKGROUND = new InnerBackground();
	private static final String DYNAMIC_LIGHT_SOURCES_KEY = "lambdynlights.menu.light_sources";
	private final DynamicLightsConfig config;
	private final Screen parent;
	private final SpruceOption entitiesOption;
	private final SpruceOption selfOption;
	private final SpruceOption blockEntitiesOption;
	private final SpruceOption waterSensitiveOption;
	private final SpruceOption creeperLightingOption;
	private final SpruceOption tntLightingOption;
	private final SpruceOption resetOption;
	private SpruceTabbedWidget tabbedWidget;

	public SettingsScreen(@Nullable Screen parent) {
		super(Text.translatable("lambdynlights.menu.title"));
		this.parent = parent;
		this.config = LambDynLights.get().config;

		this.entitiesOption = this.config.getEntitiesLightSource().getOption();
		this.selfOption = this.config.getSelfLightSource().getOption();
		this.blockEntitiesOption = this.config.getBlockEntitiesLightSource().getOption();
		this.waterSensitiveOption = this.config.getWaterSensitiveCheck().getOption();
		this.creeperLightingOption = new SpruceCyclingOption("entity.minecraft.creeper",
				amount -> this.config.setCreeperLightingMode(this.config.getCreeperLightingMode().next()),
				option -> option.getDisplayText(this.config.getCreeperLightingMode().getTranslatedText()),
				Text.translatable("lambdynlights.tooltip.creeper_lighting",
						ExplosiveLightingMode.OFF.getTranslatedText(),
						ExplosiveLightingMode.SIMPLE.getTranslatedText(),
						ExplosiveLightingMode.FANCY.getTranslatedText()));
		this.tntLightingOption = new SpruceCyclingOption("block.minecraft.tnt",
				amount -> this.config.setTntLightingMode(this.config.getTntLightingMode().next()),
				option -> option.getDisplayText(this.config.getTntLightingMode().getTranslatedText()),
				Text.translatable("lambdynlights.tooltip.tnt_lighting",
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

		var dynamicLightSources = Text.translatable(DYNAMIC_LIGHT_SOURCES_KEY);

		this.tabbedWidget = new SpruceTabbedWidget(Position.origin(), this.width, this.height, null, Math.max(100, this.width / 8), 0);
		this.tabbedWidget.getList().setBackground(RandomPrideFlagBackground.random());
		this.tabbedWidget.addTabEntry(Text.translatable("lambdynlights.menu.tabs.general"), null,
				this.tabContainerBuilder(this::buildGeneralTab));
		this.tabbedWidget.addSeparatorEntry(null);
		this.tabbedWidget.addTabEntry(Text.empty().append(dynamicLightSources).append(": ").append(this.entitiesOption.getPrefix()),
				null, this.tabContainerBuilder(this::buildEntitiesTab));
		this.tabbedWidget.addTabEntry(Text.empty().append(dynamicLightSources).append(": ").append(this.blockEntitiesOption.getPrefix()),
				null, this.tabContainerBuilder(this::buildBlockEntitiesTab));
		this.addDrawableChild(this.tabbedWidget);
	}

	private SpruceTabbedWidget.ContainerFactory tabContainerBuilder(SpruceTabbedWidget.ContainerFactory innerFactory) {
		return (width, height) -> this.buildTabContainer(width, height, innerFactory);
	}

	private SpruceContainerWidget buildTabContainer(int width, int height, SpruceTabbedWidget.ContainerFactory factory) {
		var container = new SpruceContainerWidget(Position.origin(), width, height);
		var label = new SpruceLabelWidget(Position.of(0, 18), this.title.copy().formatted(Formatting.WHITE), width);
		label.setCentered(true);
		container.addChild(label);

		var innerWidget = factory.build(width, height - this.getTextHeight() - 29
				- (LambDynLightsCompat.isCanvasInstalled() ? 43 : 0));
		innerWidget.getPosition().setRelativeY(43);
		container.addChild(innerWidget);

		container.setBackground((matrices, widget, vOffset, mouseX, mouseY, delta) -> {
			if (this.client.world != null) {
				this.fillGradient(matrices, widget.getX(), widget.getY(),
						widget.getX() + widget.getWidth(), innerWidget.getY(),
						0xc0101010, 0xd0101010);
				this.fillGradient(matrices, widget.getX(), innerWidget.getY() + innerWidget.getHeight(),
						widget.getX() + widget.getWidth(), widget.getY() + widget.getHeight(),
						0xc0101010, 0xd0101010);
			} else {
				var bg = (DirtTexturedBackground) DirtTexturedBackground.NORMAL;
				RenderUtil.renderBackgroundTexture(widget.getX(), widget.getY(),
						widget.getWidth(), innerWidget.getY() - widget.getY(),
						vOffset / 32.f, bg.red(), bg.green(), bg.blue(), bg.alpha());
				RenderUtil.renderBackgroundTexture(widget.getX(), innerWidget.getY() + innerWidget.getHeight(),
						widget.getWidth(), widget.getHeight() - (innerWidget.getY() + innerWidget.getHeight()),
						vOffset / 32.f, bg.red(), bg.green(), bg.blue(), bg.alpha());
			}
		});

		if (LambDynLightsCompat.isCanvasInstalled()) {
			var firstLine = new SpruceLabelWidget(Position.of(0, height - 29 - (5 + this.textRenderer.fontHeight) * 3),
					Text.translatable("lambdynlights.menu.canvas.1"), width);
			firstLine.setCentered(true);
			container.addChild(firstLine);
			label = new SpruceLabelWidget(Position.of(0, firstLine.getY() + firstLine.getHeight() + 5),
					Text.translatable("lambdynlights.menu.canvas.2"), width);
			label.setCentered(true);
			container.addChild(label);
		}

		container.addChild(this.resetOption.createWidget(Position.of(this, width / 2 - 155, height - 29), 150));
		container.addChild(new SpruceButtonWidget(Position.of(this, width / 2 - 155 + 160, height - 29), 150, 20,
				SpruceTexts.GUI_DONE,
				btn -> this.client.setScreen(this.parent)));

		return container;
	}

	private SpruceOptionListWidget buildGeneralTab(int width, int height) {
		var list = new SpruceOptionListWidget(Position.of(0, 0), width, height);
		list.setBackground(INNER_BACKGROUND);
		list.addSingleOptionEntry(this.config.dynamicLightsModeOption);
		list.addSingleOptionEntry(new SpruceSeparatorOption(DYNAMIC_LIGHT_SOURCES_KEY, true, null));
		list.addOptionEntry(this.entitiesOption, this.blockEntitiesOption);
		list.addOptionEntry(this.selfOption, this.waterSensitiveOption);
		list.addOptionEntry(this.creeperLightingOption, this.tntLightingOption);
		return list;
	}

	private LightSourceListWidget buildEntitiesTab(int width, int height) {
		return this.buildLightSourcesTab(width, height, Registries.ENTITY_TYPE.stream().map(DynamicLightHandlerHolder::cast).collect(Collectors.toList()));
	}

	private LightSourceListWidget buildBlockEntitiesTab(int width, int height) {
		return this.buildLightSourcesTab(width, height, Registries.BLOCK_ENTITY_TYPE.stream().map(DynamicLightHandlerHolder::cast).collect(Collectors.toList()));
	}

	private LightSourceListWidget buildLightSourcesTab(int width, int height, List<DynamicLightHandlerHolder<?>> entries) {
		var list = new LightSourceListWidget(Position.of(0, 0), width, height);
		list.setBackground(INNER_BACKGROUND);
		list.addAll(entries);
		return list;
	}

	@Override
	public void renderBackground(MatrixStack matrices) {
	}
}
