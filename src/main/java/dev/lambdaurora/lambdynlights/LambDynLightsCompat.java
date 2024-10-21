/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Represents a utility class for compatibility.
 *
 * @author LambdAurora
 * @version 3.1.0
 * @since 1.0.0
 */
public final class LambDynLightsCompat {
	/**
	 * Returns whether Canvas is installed.
	 *
	 * @return {@code true} if Canvas is installed, else {@code false}
	 */
	public static boolean isCanvasInstalled() {
		return FabricLoader.getInstance().isModLoaded("canvas");
	}

	/**
	 * {@return {@code true} if Sodium is installed, or {@code false} otherwise}
	 */
	public static boolean isSodiumInstalled() {
		return FabricLoader.getInstance().isModLoaded("sodium");
	}
}
