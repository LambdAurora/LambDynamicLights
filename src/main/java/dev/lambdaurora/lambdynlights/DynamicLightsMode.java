/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.util.Nameable;
import net.minecraft.TextFormatting;
import net.minecraft.network.chat.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents the dynamic lights mode.
 *
 * @author LambdAurora
 * @version 2.0.1
 * @since 1.0.0
 */
public enum DynamicLightsMode implements Nameable {
	OFF(0, TextFormatting.RED, SpruceTexts.OPTIONS_OFF),
	FASTEST(500, TextFormatting.GOLD, SpruceTexts.OPTIONS_GENERIC_FASTEST),
	FAST(250, TextFormatting.YELLOW, SpruceTexts.OPTIONS_GENERIC_FAST),
	FANCY(0, TextFormatting.GREEN, SpruceTexts.OPTIONS_GENERIC_FANCY);

	private final int delay;
	private final Text translatedText;

	DynamicLightsMode(int delay, @NotNull TextFormatting formatting, @NotNull Text translatedText) {
		this.delay = delay;
		this.translatedText = translatedText.copy().withStyle(formatting);
	}

	/**
	 * Returns whether this mode enables dynamic lights.
	 *
	 * @return {@code true} if the mode enables dynamic lights, else {@code false}
	 */
	public boolean isEnabled() {
		return this != OFF;
	}

	/**
	 * Returns whether this mode has an update delay.
	 *
	 * @return {@code true} if the mode has an update delay, else {@code false}
	 */
	public boolean hasDelay() {
		return this.delay != 0;
	}

	/**
	 * Returns the update delay of this mode.
	 *
	 * @return the mode's update delay
	 */
	public int getDelay() {
		return this.delay;
	}

	/**
	 * Returns the next dynamic lights mode available.
	 *
	 * @return the next available dynamic lights mode
	 */
	public DynamicLightsMode next() {
		DynamicLightsMode[] v = values();
		if (v.length == this.ordinal() + 1)
			return v[0];
		return v[this.ordinal() + 1];
	}

	/**
	 * Returns the translated text of the dynamic lights mode.
	 *
	 * @return the translated text of the dynamic lights mode
	 */
	public @NotNull Text getTranslatedText() {
		return this.translatedText;
	}

	@Override
	public @NotNull String getName() {
		return this.name().toLowerCase();
	}

	/**
	 * Gets the dynamic lights mode from its identifier.
	 *
	 * @param id the identifier of the dynamic lights mode
	 * @return the dynamic lights mode if found, else empty
	 */
	public static @NotNull Optional<DynamicLightsMode> byId(@NotNull String id) {
		return Arrays.stream(values()).filter(mode -> mode.getName().equalsIgnoreCase(id)).findFirst();
	}
}
