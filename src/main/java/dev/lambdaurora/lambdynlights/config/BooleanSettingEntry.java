/*
 * Copyright © 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.config;

import com.electronwill.nightconfig.core.Config;
import dev.lambdaurora.spruceui.option.SpruceBooleanOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BooleanSettingEntry extends SettingEntry<Boolean> {
	public BooleanSettingEntry(String key, boolean defaultValue, @Nullable Config config, @Nullable Text tooltip) {
		super(key, defaultValue, config, tooltip);
	}

	public BooleanSettingEntry(String key, boolean defaultValue, @Nullable Config config) {
		super(key, defaultValue, config);
	}

	@Override
	protected void deserialize(Object obj) {
	}

	@Override
	protected Object serialize() {
		return this.get();
	}

	@Override
	public void load(Config config) {
		this.config = config;
		this.set(this.config.getOrElse(this.key(), this.get()));
	}

	@Override
	public BooleanSettingEntry withOnSet(@Nullable Consumer<Boolean> onSet) {
		this.onSet = onSet;
		return this;
	}

	@Override
	protected SpruceOption buildOption(@Nullable Text tooltip) {
		return new SpruceBooleanOption(
				this.getOptionKey(),
				this::get,
				this::set,
				tooltip,
				true
		);
	}
}
