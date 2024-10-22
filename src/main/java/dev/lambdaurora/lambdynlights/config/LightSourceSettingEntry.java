/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.config;

import com.electronwill.nightconfig.core.Config;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.option.SpruceToggleBooleanOption;
import net.minecraft.network.chat.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LightSourceSettingEntry extends BooleanSettingEntry {
	public LightSourceSettingEntry(String key, boolean defaultValue, @Nullable Config config, @Nullable Text tooltip) {
		super(key, defaultValue, config, tooltip);
	}

	public LightSourceSettingEntry(String key, boolean defaultValue, @Nullable Config config) {
		super(key, defaultValue, config);
	}

	@Override
	protected SpruceOption buildOption(@Nullable Text tooltip) {
		return new Option(
				this.key(),
				this::get,
				this::set,
				tooltip
		);
	}

	public static final class Option extends SpruceToggleBooleanOption {
		public Option(String key, Supplier<Boolean> getter, Consumer<Boolean> setter, @Nullable Text tooltip) {
			super(key, getter, setter, tooltip, false);
		}

		@Override
		public Text getDisplayText() {
			boolean value = this.get();
			Text toggleText = SpruceTexts.getToggleText(value);

			return this.getDisplayText(toggleText);
		}

		@Override
		public Text getDisplayText(Text value) {
			return Text.translatable("spruceui.options.generic", this.getPrefix(), value);
		}
	}
}
