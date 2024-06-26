/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.gui;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class DynamicLightsOptionsOption {
	private static final String KEY = "lambdynlights.menu.title";

	public static SimpleOption<Unit> getOption(Screen parent) {
		return new SimpleOption<>(
				KEY, SimpleOption.emptyTooltip(),
				(title, object) -> title,
				new DummyValueSet(parent),
				Unit.INSTANCE,
				unit -> {});
	}

	private record DummyValueSet(Screen parent) implements SimpleOption.Callbacks<Unit> {

		@Override
		public Function<SimpleOption<Unit>, ClickableWidget> getWidgetCreator(SimpleOption.TooltipFactory<Unit> tooltipSupplier, GameOptions options,
				int x, int y, int width, Consumer<Unit> changeCallback) {
			return option -> ButtonWidget.builder(Text.translatable(KEY), button -> MinecraftClient.getInstance()
					.setScreen(new SettingsScreen(this.parent))).dimensions(x, y, width, 20).build();
		}

		@Override
		public Optional<Unit> validate(Unit value) {
			return Optional.of(Unit.INSTANCE);
		}

		@Override
		public Codec<Unit> codec() {
			return Codec.EMPTY.codec();
		}
	}
}
