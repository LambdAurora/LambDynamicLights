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
import dev.lambdaurora.spruceui.tooltip.TooltipData;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LightSourceSettingEntry extends BooleanSettingEntry {
	public LightSourceSettingEntry(
			String key, String guiKey, boolean defaultValue, @Nullable Config config, TooltipData tooltip
	) {
		super("light_sources.settings.entities." + key, guiKey, defaultValue, config, tooltip);
	}

	@Override
	protected SpruceOption buildOption(TooltipData tooltip) {
		return new Option(
				this.guiKey(),
				this::get,
				this::set,
				tooltip
		);
	}

	public static final class Option extends SpruceToggleBooleanOption {
		public Option(String key, Supplier<Boolean> getter, Consumer<Boolean> setter, TooltipData tooltip) {
			super(key, getter, setter, tooltip, false);
		}

		@Override
		public Component getDisplayText() {
			boolean value = this.get();
			Component toggleText = SpruceTexts.getToggleText(value);

			return this.getDisplayText(toggleText);
		}

		@Override
		public Component getDisplayText(Component value) {
			return Component.translatable("spruceui.options.generic", this.getPrefix(), value);
		}
	}
}
