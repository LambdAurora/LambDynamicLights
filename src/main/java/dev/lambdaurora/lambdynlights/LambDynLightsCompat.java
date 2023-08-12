/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Represents a utility class for compatibility.
 *
 * @author LambdAurora
 * @version 1.3.3
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
	 * Returns whether Lil Tater Reloaded is installed.
	 *
	 * @return {@code true} if LTR is installed, else {@code false}
	 */
	public static boolean isLilTaterReloadedInstalled() {
		// Don't even think about it Yog.
		return FabricLoader.getInstance().isModLoaded("ltr");
	}

	/**
	 * Returns whether Sodium 0.1.0 is installed.
	 *
	 * @return {@code true} if Sodium 0.1.0 is installed, else {@code false}
	 */
	public static boolean isSodium010Installed() {
		return FabricLoader.getInstance().getModContainer("sodium").map(mod -> mod.getMetadata().getVersion().getFriendlyString().startsWith("0.1.0"))
				.orElse(false);
	}

	public static boolean isSodium05XInstalled() {
		return FabricLoader.getInstance().getModContainer("sodium").map(mod -> mod.getMetadata().getVersion().getFriendlyString().startsWith("0.5."))
				.orElse(false);
	}
}
