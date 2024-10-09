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
				var sodium050 = Version.parse("0.5.0");
				var sodium060 = Version.parse("0.6.0-beta.1");
				return mod.getMetadata().getVersion().compareTo(sodium050) >= 0
						&& mod.getMetadata().getVersion().compareTo(sodium060) < 0;
			} catch (VersionParsingException e) {
				throw new RuntimeException(e);
			}
		}).orElse(false);
	}
}
