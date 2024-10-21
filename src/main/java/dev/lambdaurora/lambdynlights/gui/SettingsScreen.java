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
import dev.lambdaurora.lambdynlights.accessor.DynamicLightHandlerHolder;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.background.Background;
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.option.SpruceSeparatorOption;
import dev.lambdaurora.spruceui.option.SpruceSimpleActionOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceParentWidget;
import dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget;
import dev.lambdaurora.spruceui.widget.text.SpruceTextFieldWidget;
import net.minecraft.TextFormatting;
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
 * @version 3.2.0
 * @since 1.0.0
 */
public class SettingsScreen extends SpruceScreen {
	private static final Background INNER_BACKGROUND = new InnerBackground();
	private static final String DYNAMIC_LIGHT_SOURCES_KEY = "lambdynlights.menu.light_sources";
	private final DynamicLightsConfig config;
	private final Screen parent;
	private final SpruceOption entitiesOption;
	private final SpruceOption selfOption;
	private final SpruceOption waterSensitiveOption;
	private final SpruceOption creeperLightingOption;
	private final SpruceOption tntLightingOption;
	private final SpruceOption resetOption;
	private SpruceTabbedWidget tabbedWidget;
	private SpruceTextFieldWidget searchInput;

	public SettingsScreen(@Nullable Screen parent) {
		super(Text.translatable("lambdynlights.menu.title"));
		this.parent = parent;
		this.config = LambDynLights.get().config;

		this.entitiesOption = this.config.getEntitiesLightSource().getOption();
		this.selfOption = this.config.getSelfLightSource().getOption();
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
			var client = Minecraft.getInstance();
			this.init(client, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
		});
	}

	@Override
	public void removed() {
		super.removed();
		this.config.save();
	}

	private int getTextHeight() {
		return (5 + this.font.lineHeight) * 3 + 5;
	}

	@Override
	protected void init() {
		super.init();

		var dynamicLightSources = Text.translatable(DYNAMIC_LIGHT_SOURCES_KEY);

		int tabIndex = 0;
		if (this.tabbedWidget != null) {
			var oldEntry = this.tabbedWidget.getList().getCurrentTab();
			tabIndex = this.tabbedWidget.getList().children().indexOf(oldEntry);
		}

		this.tabbedWidget = new SpruceTabbedWidget(Position.origin(), this.width, this.height, this.title.copy(), Math.max(100, this.width / 8));
		this.tabbedWidget.getList().setBackground(RandomPrideFlagBackground.random());
		this.tabbedWidget.addTabEntry(Text.translatable("lambdynlights.menu.tabs.general"), null,
				this.tabContainerBuilder(this::buildGeneralTab));
		this.tabbedWidget.addSeparatorEntry(null);
		this.tabbedWidget.addTabEntry(Text.empty().append(dynamicLightSources).append(": ").append(this.entitiesOption.getPrefix()),
				null, this.tabContainerBuilder(this::buildEntitiesTab));
		this.addWidget(this.tabbedWidget);

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
				height - this.tabbedWidget.getList().getPosition().getRelativeY() - 40
		));

		container.addChild(this.resetOption.createWidget(Position.of(this, width / 2 - 155, height - 29), 150));
		container.addChild(new SpruceButtonWidget(Position.of(this, width / 2 - 155 + 160, height - 29), 150, 20,
				SpruceTexts.GUI_DONE,
				btn -> this.client.setScreen(this.parent)
		));

		return container;
	}

	private void buildGeneralTab(TabContext context) {
		var list = new SpruceOptionListWidget(Position.of(0, 0), context.width(), context.height());
		list.addSingleOptionEntry(this.config.dynamicLightsModeOption);
		list.addSingleOptionEntry(new SpruceSeparatorOption(DYNAMIC_LIGHT_SOURCES_KEY, true, null));
		list.addOptionEntry(this.entitiesOption, this.selfOption);
		list.addOptionEntry(this.waterSensitiveOption, null);
		list.addOptionEntry(this.creeperLightingOption, this.tntLightingOption);
		context.addInnerWidget(list);
	}

	private void buildEntitiesTab(TabContext context) {
		this.buildLightSourcesTab(context, BuiltInRegistries.ENTITY_TYPE.stream().map(DynamicLightHandlerHolder::cast).collect(Collectors.toList()));
	}

	private void buildLightSourcesTab(TabContext context, List<DynamicLightHandlerHolder<?>> entries) {
		var oldSearch = this.searchInput != null ? this.searchInput.getText() : "";
		this.searchInput = context.addSearchInput();
		var list = new LightSourceListWidget(Position.of(0, 0), context.width(), context.height(), this.searchInput);
		list.addAll(entries);
		context.addInnerWidget(list);
		this.searchInput.setText(oldSearch);
	}

	record TabContext(SpruceTabbedWidget tabbedWidget, SpruceContainerWidget container, int height) {
		int width() {
			return this.container().getWidth();
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
			this.container().addChild(searchInput);
			return searchInput;
		}

		void addInnerWidget(SpruceParentWidget<?> widget) {
			widget.getPosition().setRelativeY(this.tabbedWidget.getList().getPosition().getRelativeY());
			this.container().addChild(widget);
		}
	}
}
