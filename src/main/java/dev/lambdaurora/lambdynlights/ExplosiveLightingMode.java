/*
 * Copyright � 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.util.Nameable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents the explosives dynamic lighting mode.
 *
 * @author LambdAurora
 * @version 2.0.1
 * @since 1.2.1
 */
public enum ExplosiveLightingMode implements Nameable {
	OFF(Formatting.RED, SpruceTexts.OPTIONS_OFF),
	SIMPLE(Formatting.YELLOW, SpruceTexts.OPTIONS_GENERIC_SIMPLE),
	FANCY(Formatting.GREEN, SpruceTexts.OPTIONS_GENERIC_FANCY);

	private final Text translatedText;

	ExplosiveLightingMode(@NotNull Formatting formatting, @NotNull Text translatedText) {
		this.translatedText = translatedText.copy().formatted(formatting);
	}

	/**
	 * Returns whether this mode enables explosives dynamic lighting.
	 *
	 * @return {@code true} if the mode enables explosives dynamic lighting, else {@code false}
	 */
	public boolean isEnabled() {
		return this != OFF;
	}

	/**
	 * Returns the next explosives dynamic lighting mode available.
	 *
	 * @return the next available explosives dynamic lighting mode
	 */
	public ExplosiveLightingMode next() {
		ExplosiveLightingMode[] v = values();
		if (v.length == this.ordinal() + 1)
			return v[0];
		return v[this.ordinal() + 1];
	}

	/**
	 * Returns the translated text of the explosives dynamic lighting mode.
	 *
	 * @return the translated text of the explosives dynamic lighting mode
	 */
	public @NotNull Text getTranslatedText() {
		return this.translatedText;
	}

	@Override
	public @NotNull String getName() {
		return this.name().toLowerCase();
	}

	/**
	 * Gets the explosives dynamic lighting mode from its identifier.
	 *
	 * @param id the identifier of the explosives dynamic lighting mode
	 * @return the explosives dynamic lighting mode if found, else empty
	 */
	public static @NotNull Optional<ExplosiveLightingMode> byId(@NotNull String id) {
		return Arrays.stream(values()).filter(mode -> mode.getName().equalsIgnoreCase(id)).findFirst();
	}
}
