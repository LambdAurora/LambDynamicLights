/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import dev.lambdaurora.lambdynlights.engine.TickMode;
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
 * @version 4.8.0
 * @since 1.0.0
 */
public enum DynamicLightsMode implements Nameable {
	OFF(TickMode.REAL_TIME, TextFormatting.RED, SpruceTexts.OPTIONS_OFF),
	FASTEST(TickMode.SLOWER, TextFormatting.GOLD, SpruceTexts.OPTIONS_GENERIC_FASTEST),
	FAST(TickMode.SLOW, TextFormatting.YELLOW, SpruceTexts.OPTIONS_GENERIC_FAST),
	FANCY(TickMode.REAL_TIME, TextFormatting.GREEN, SpruceTexts.OPTIONS_GENERIC_FANCY);

	private final TickMode tickMode;
	private final Text translatedText;

	DynamicLightsMode(TickMode tickMode, @NotNull TextFormatting formatting, @NotNull Text translatedText) {
		this.tickMode = tickMode;
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
	 * {@return the corresponding tick mode for this dynamic lights mode}
	 */
	public @NotNull TickMode tickMode() {
		return this.tickMode;
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
