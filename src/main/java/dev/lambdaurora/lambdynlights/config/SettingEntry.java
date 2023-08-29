/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.config;

import com.electronwill.nightconfig.core.Config;
import dev.lambdaurora.spruceui.option.SpruceOption;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class SettingEntry<T> {
	private final String key;
	private final SpruceOption option;
	protected @Nullable Config config;
	private T value;
	protected @Nullable Consumer<T> onSet;

	protected SettingEntry(String key, T defaultValue, @Nullable Config config, @Nullable Text tooltip) {
		this.key = key;
		this.config = config;
		this.option = this.buildOption(tooltip);
		this.value = defaultValue;
	}

	protected SettingEntry(String key, T defaultValue, @Nullable Config config) {
		this(key, defaultValue, config, null);
	}

	public String key() {
		return this.key;
	}

	public T get() {
		return this.value;
	}

	public void set(T value) {
		this.value = value;

		if (this.onSet != null)
			this.onSet.accept(value);

		this.save();
	}

	protected abstract void deserialize(Object obj);

	protected abstract Object serialize();

	public void load(Config config) {
		this.config = config;
		this.deserialize(this.config.getOrElse(this.key(), this.serialize()));
	}

	public void save() {
		if (this.config != null)
			this.config.set(this.key(), this.serialize());
	}

	public SettingEntry<T> withOnSet(@Nullable Consumer<T> onSet) {
		this.onSet = onSet;
		return this;
	}

	public SpruceOption getOption() {
		return this.option;
	}

	protected String getOptionKey() {
		return "lambdynlights.option." + this.key();
	}

	protected abstract SpruceOption buildOption(@Nullable Text tooltip);
}
