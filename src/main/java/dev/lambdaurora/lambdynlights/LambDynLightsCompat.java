/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import dev.yumi.mc.core.api.YumiMods;

/**
 * Represents a utility class for compatibility.
 *
 * @author LambdAurora
 * @version 4.8.3
 * @since 1.0.0
 */
public final class LambDynLightsCompat {
	/**
	 * Returns whether Canvas is installed.
	 *
	 * @return {@code true} if Canvas is installed, else {@code false}
	 */
	public static boolean isCanvasInstalled() {
		return YumiMods.get().isModLoaded("canvas");
	}

	/**
	 * {@return {@code true} if Sodium is installed, or {@code false} otherwise}
	 */
	public static boolean isSodiumInstalled() {
		return YumiMods.get().isModLoaded("sodium");
	}

	/**
	 * {@return {@code true} if RSO is installed, or {@code false} otherwise}
	 */
	public static boolean isRSOInstalled() {
		return YumiMods.get().isModLoaded("reeses-sodium-options") || YumiMods.get().isModLoaded("reeses_sodium_options");
	}

	/**
	 * {@return {@code true} if Sodium Options API is installed, or {@code false} otherwise}
	 */
	public static boolean isSodiumOptionsAPIInstalled() {
		return YumiMods.get().isModLoaded("sodiumoptionsapi");
	}
}
