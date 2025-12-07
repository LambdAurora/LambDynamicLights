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
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.tooltip.TooltipData;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public abstract class SettingEntry<T> {
	private final String key;
	private final String guiKey;
	private final SpruceOption option;
	private final T defaultValue;
	protected @Nullable Config config;
	private T value;
	protected @Nullable Consumer<T> onSet;

	protected SettingEntry(String key, String guiKey, T defaultValue, @Nullable Config config, TooltipData tooltip) {
		this.key = key;
		this.guiKey = guiKey;
		this.defaultValue = defaultValue;
		this.config = config;
		this.option = this.buildOption(tooltip);
		this.value = defaultValue;
	}

	protected SettingEntry(String key, T defaultValue, @Nullable Config config) {
		this(key, key, defaultValue, config, TooltipData.EMPTY);
	}

	public String key() {
		return this.key;
	}

	protected String guiKey() {
		return this.guiKey;
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

	public void reset() {
		this.set(this.defaultValue);
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

	protected abstract SpruceOption buildOption(TooltipData tooltip);
}
