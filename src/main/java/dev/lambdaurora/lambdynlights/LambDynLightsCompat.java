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
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

/**
 * Represents a utility class for compatibility.
 *
 * @author LambdAurora
 * @version 3.0.0
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

	public static boolean isSodium05XInstalled() {
		return FabricLoader.getInstance().getModContainer("sodium").map(mod -> {
			try {
				return mod.getMetadata().getVersion().compareTo(Version.parse("0.5.0")) >= 0;
			} catch (VersionParsingException e) {
				throw new RuntimeException(e);
			}
		}).orElse(false);
	}
}
