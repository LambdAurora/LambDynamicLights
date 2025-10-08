/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.gui;

import dev.lambdaurora.lambdynlights.DynamicLightsConfig;
import dev.lambdaurora.lambdynlights.ExplosiveLightingMode;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import dev.lambdaurora.lambdynlights.accessor.DynamicLightHandlerHolder;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTextAlignment;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.option.SpruceSimpleActionOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceParentWidget;
import dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget;
import dev.lambdaurora.spruceui.widget.text.SpruceTextFieldWidget;
import net.minecraft.TextFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Text;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents the settings screen of LambDynamicLights.
 *
 * @author LambdAurora
 * @version 4.8.0
 * @since 1.0.0
 */
public class SettingsScreen extends SpruceScreen {
	public static final Text MOD_NAME = Text.translatable("lambdynlights");
	public static final Text TITLE = Text.translatable("lambdynlights.menu.title", MOD_NAME);
	private static final Text VERSION;
	private static final String DYNAMIC_LIGHT_SOURCES_KEY = "lambdynlights.menu.light_sources";
	private static final String SPECIAL_DYNAMIC_LIGHT_SOURCES_KEY = "lambdynlights.menu.light_sources.special";
	private final DynamicLightsConfig config;
	private final Screen parent;
	private final SpruceOption entitiesOption;
	private final SpruceOption selfOption;
	private final SpruceOption waterSensitiveOption;
	private final SpruceOption creeperLightingOption;
	private final SpruceOption tntLightingOption;
	private final SpruceOption debugCellDisplayRadiusOption;
	private final SpruceOption debugLightLevelRadiusOption;
	private final SpruceOption resetOption;
	private SpruceTabbedWidget tabbedWidget;
	private SpruceTextFieldWidget searchInput;

	static {
		String rawVersion = LambDynLightsConstants.VERSION;

		if (rawVersion.endsWith("-local")) {
			rawVersion = rawVersion.substring(0, rawVersion.length() - "-local".length());
		}

		var version = Text.literal('v' + rawVersion).withStyle(TextFormatting.GRAY);

		if (rawVersion.matches("^.+-rc\\.\\d+\\+.+$")) {
			version = version.append(Text.literal(" (Release Candidate)").withStyle(TextFormatting.GOLD));
		}

		if (LambDynLightsConstants.isDevMode()) {
			version = version.append(Text.literal(" (dev)").withStyle(TextFormatting.RED));
		}

		VERSION = version;
	}

	public SettingsScreen(@Nullable Screen parent) {
		super(TITLE);
		this.parent = parent;
		this.config = LambDynLights.get().config;

		this.entitiesOption = this.config.getEntitiesLightSource().getOption();
		this.selfOption = this.config.getSelfLightSource().getOption();
		this.waterSensitiveOption = this.config.getWaterSensitiveCheck().getOption();
		this.creeperLightingOption = SpruceOption.cyclingBuilder("entity.minecraft.creeper",
						amount -> this.config.setCreeperLightingMode(this.config.getCreeperLightingMode().next()),
						option -> option.getDisplayText(this.config.getCreeperLightingMode().getTranslatedText())
				)
				.tooltip(Text.translatable("lambdynlights.tooltip.creeper_lighting",
						ExplosiveLightingMode.OFF.getTranslatedText(),
						ExplosiveLightingMode.SIMPLE.getTranslatedText(),
						ExplosiveLightingMode.FANCY.getTranslatedText()
				)).build();
		this.tntLightingOption = SpruceOption.cyclingBuilder("block.minecraft.tnt",
						amount -> this.config.setTntLightingMode(this.config.getTntLightingMode().next()),
						option -> option.getDisplayText(this.config.getTntLightingMode().getTranslatedText())
				)
				.tooltip(Text.translatable("lambdynlights.tooltip.tnt_lighting",
						ExplosiveLightingMode.OFF.getTranslatedText(),
						ExplosiveLightingMode.SIMPLE.getTranslatedText(),
						ExplosiveLightingMode.FANCY.getTranslatedText()
				)).build();
		this.debugCellDisplayRadiusOption = SpruceOption.doubleBuilder("lambdynlights.option.debug.cell_display_radius",
				0,
				10,
				1,
				() -> (double) this.config.getDebugCellDisplayRadius(),
				value -> this.config.setDebugCellDisplayRadius(value.intValue()),
				option -> option.getDisplayText(
						option.get() <= 0
								? SpruceTexts.OPTIONS_OFF.copy().withStyle(TextFormatting.RED)
								: Text.literal(String.format("%.0f", option.get()))
				)
		).build();
		this.debugLightLevelRadiusOption = SpruceOption.doubleBuilder("lambdynlights.option.debug.light_level_radius",
				0,
				10,
				1,
				() -> (double) this.config.getDebugLightLevelRadius(),
				value -> this.config.setDebugLightLevelRadius(value.intValue()),
				option -> option.getDisplayText(
						option.get() <= 0
								? SpruceTexts.OPTIONS_OFF.copy().withStyle(TextFormatting.RED)
								: Text.literal(String.format("%.0f", option.get()))
				)
		).build();
		this.resetOption = SpruceSimpleActionOption.reset(btn -> {
			this.config.reset();
			var client = Minecraft.getInstance();
			this.init(client, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
		});
	}

	@Override
	public void removed() {
		super.removed();
		this.config.save();
	}

	@Override
	protected void init() {
		super.init();

		int tabIndex = 0;
		if (this.tabbedWidget != null) {
			var oldEntry = this.tabbedWidget.getList().getCurrentTab();
			tabIndex = this.tabbedWidget.getList().children().indexOf(oldEntry);
		}

		this.tabbedWidget = new SpruceTabbedWidget(Position.origin(), this.width, this.height - 40, this.title.copy(), Math.max(100, this.width / 8));
		this.tabbedWidget.getList().setBackground(RandomPrideFlagBackground.random());
		this.tabbedWidget.addTabEntry(Text.translatable("lambdynlights.menu.tabs.general"), null,
				this.tabContainerBuilder(this::buildGeneralTab)
		);
		this.tabbedWidget.addTabEntry(Text.translatable("lambdynlights.menu.tabs.performance"), null,
				this.tabContainerBuilder(this::buildPerformanceTab)
		);
		this.tabbedWidget.addSeparatorEntry(Text.translatable(DYNAMIC_LIGHT_SOURCES_KEY));
		this.tabbedWidget.addTabEntry(
				this.entitiesOption.getPrefix(), null,
				this.tabContainerBuilder(this::buildEntitiesTab)
		);
		this.tabbedWidget.addTabEntry(
				Text.translatable("lambdynlights.menu.tabs.dynamic_lights.special"),
				null,
				this.tabContainerBuilder(this::buildSpecialTab)
		);
		this.tabbedWidget.addSeparatorEntry(Text.translatable("lambdynlights.menu.tabs.advanced"));
		this.tabbedWidget.addTabEntry(
				Text.translatable("lambdynlights.menu.tabs.debug"),
				Text.translatable("lambdynlights.menu.tabs.debug.description").withStyle(TextFormatting.GRAY),
				this.tabContainerBuilder(this::buildDebugTab)
		);
		this.addRenderableWidget(this.tabbedWidget);

		int tabsWidth = this.tabbedWidget.getList().getWidth();
		var resetButtonPos = Position.of(this, tabsWidth + (this.width - tabsWidth) / 2 - 155, this.height - 29);

		var supportMeButton = new SpruceButtonWidget(
				Position.of(this, 4, resetButtonPos.getRelativeY()),
				tabsWidth - 8, 20, Text.translatable("lambdynlights.menu.support_me"),
				btn -> Util.getPlatform().openUri("https://donate.lambdaurora.dev/")
		);
		supportMeButton.setTooltip(Text.translatable("lambdynlights.menu.support_me.tooltip"));
		this.addRenderableWidget(supportMeButton);

		this.addRenderableWidget(this.resetOption.createWidget(resetButtonPos, 150));
		var doneButtonPos = resetButtonPos.copy();
		doneButtonPos.setRelativeX(doneButtonPos.getRelativeX() + 160);
		this.addRenderableWidget(new SpruceButtonWidget(doneButtonPos, 150, 20,
				SpruceTexts.GUI_DONE,
				btn -> this.client.setScreen(this.parent)
		));

		if (tabIndex > 0 && this.tabbedWidget.getList().children().get(tabIndex) instanceof SpruceTabbedWidget.TabEntry tabEntry) {
			this.tabbedWidget.getList().setSelected(tabEntry);
		}
	}

	private SpruceTabbedWidget.ContainerFactory tabContainerBuilder(Consumer<TabContext> innerFactory) {
		return (width, height) -> this.buildTabContainer(width, height, innerFactory);
	}

	private SpruceContainerWidget buildTabContainer(int width, int height, Consumer<TabContext> tabConsumer) {
		var container = new SpruceContainerWidget(Position.origin(), width, height);

		tabConsumer.accept(new TabContext(
				this.tabbedWidget,
				container,
				height - this.tabbedWidget.getList().getPosition().getRelativeY()
		));

		return container;
	}

	private void buildGeneralTab(TabContext context) {
		context.addVersionLabel();

		var list = new SpruceOptionListWidget(Position.origin(), context.width(), context.height());
		list.addSingleOptionEntry(this.config.dynamicLightsModeOption);
		list.addOptionEntry(this.entitiesOption, this.selfOption);
		list.addSingleOptionEntry(this.waterSensitiveOption);
		context.addInnerWidget(list);
	}

	private void buildPerformanceTab(TabContext context) {
		context.addVersionLabel();

		var list = new SpruceOptionListWidget(Position.origin(), context.width(), context.height());
		list.addSingleOptionEntry(this.config.chunkRebuildSchedulerOption);
		list.addSingleOptionEntry(this.config.slowTickingOption);
		list.addSingleOptionEntry(this.config.slowerTickingOption);
		list.addSingleOptionEntry(this.config.getBackgroundAdaptativeTicking().getOption());
		context.addInnerWidget(list);
	}

	private void buildDebugTab(TabContext context) {
		context.addVersionLabel();

		var list = new SpruceOptionListWidget(Position.origin(), context.width(), context.height());
		list.addSingleOptionEntry(this.config.getDebugActiveDynamicLightingCells().getOption());
		list.addSingleOptionEntry(this.debugCellDisplayRadiusOption);
		list.addSingleOptionEntry(this.config.getDebugDisplayDynamicLightingChunkRebuilds().getOption());
		list.addSingleOptionEntry(this.debugLightLevelRadiusOption);
		list.addSingleOptionEntry(this.config.getDebugDisplayHandlerBoundingBox().getOption());
		context.addInnerWidget(list);
	}

	private void buildEntitiesTab(TabContext context) {
		this.buildLightSourcesTab(context,
				BuiltInRegistries.ENTITY_TYPE.stream()
						.map(DynamicLightHandlerHolder::cast)
						.collect(Collectors.toList())
		);
	}

	private void buildSpecialTab(TabContext context) {
		context.addVersionLabel();

		var list = new SpruceOptionListWidget(Position.origin(), context.width(), context.height());
		list.addOptionEntry(this.creeperLightingOption, this.tntLightingOption);
		list.addOptionEntry(this.config.getBeamLighting().getOption(), this.config.getFireflyLighting().getOption());
		list.addOptionEntry(this.config.getGuardianLaser().getOption(), this.config.getSonicBoomLighting().getOption());
		list.addSmallSingleOptionEntry(this.config.getGlowingEffectLighting().getOption());
		context.addInnerWidget(list);
	}

	private void buildLightSourcesTab(TabContext context, List<DynamicLightHandlerHolder<?>> entries) {
		var oldSearch = this.searchInput != null ? this.searchInput.getText() : "";
		this.searchInput = context.addSearchInput();
		this.searchInput.setTooltip(Text.translatable(
				"lambdynlights.menu.tabs.dynamic_lights.entity.search.tooltip",
				Text.literal("@").withStyle(TextFormatting.AQUA),
				Text.literal("@minecraft").withStyle(TextFormatting.AQUA),
				Text.literal("$").append(SpruceTexts.OPTIONS_ON).withStyle(TextFormatting.GOLD),
				Text.literal("$").append(SpruceTexts.OPTIONS_OFF).withStyle(TextFormatting.GOLD)
		));
		var list = new LightSourceListWidget(Position.origin(), context.width(), context.height(), this.searchInput);
		list.addAll(entries);
		context.addInnerWidget(list);
		this.searchInput.setText(oldSearch);
	}

	record TabContext(SpruceTabbedWidget tabbedWidget, SpruceContainerWidget container, int height) {
		int width() {
			return this.container().getWidth();
		}

		void addVersionLabel() {
			this.addWidget(new SpruceLabelWidget(
					Position.of(0, 16), VERSION,
					this.width() - 4, SpruceTextAlignment.RIGHT
			));
		}

		SpruceTextFieldWidget addSearchInput() {
			var searchText = Text.translatable("lambdynlights.menu.search");
			var textWidth = Minecraft.getInstance().font.width(searchText);
			int searchInputX = this.width() - 140;

			this.container.addChild(new SpruceLabelWidget(Position.of(searchInputX - 4 - textWidth, 8), searchText, textWidth));

			var searchInput = SpruceTextFieldWidget.builder(Position.of(searchInputX, 4), 136, 20)
					.title(Text.literal("Search"))
					.placeholder(EntityType.BLAZE.getDescription().copy().withStyle(TextFormatting.GRAY, TextFormatting.ITALIC))
					.build();
			return this.addWidget(searchInput);
		}

		<T extends SpruceWidget> T addWidget(T widget) {
			this.container().addChild(widget);
			return widget;
		}

		void addInnerWidget(SpruceParentWidget<?> widget) {
			widget.getPosition().setRelativeY(this.tabbedWidget.getList().getPosition().getRelativeY());
			this.addWidget(widget);
		}
	}
}
