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
 * @version 4.8.9
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
		return YumiMods.get().getMod("sodium")
				.map(mod -> {
					try {
						var version = mod.getVersionString();

						int dashSeparator = version.indexOf('-');
						int metadataSeparator = version.indexOf('+');

						if (dashSeparator > -1 && dashSeparator < metadataSeparator) {
							version = version.substring(0, dashSeparator);
						} else if (metadataSeparator > -1) {
							version = version.substring(0, metadataSeparator);
						}

						var parts = version.split("\\.");

						// We consider that Sodium is here for the mixins if <=0.6.x, 0.8.x+ has an API.
						if (Integer.parseInt(parts[0]) > 0) return false;
						else return Integer.parseInt(parts[1]) <= 6;
					} catch (Exception e) {
						return false;
					}
				})
				.orElse(false);
	}

	/**
	 * {@return {@code true} if RSO is installed, or {@code false} otherwise}
	 */
	public static boolean isRSOInstalled() {
		return isSodiumInstalled() && (YumiMods.get().isModLoaded("reeses-sodium-options") || YumiMods.get().isModLoaded("reeses_sodium_options"));
	}

	/**
	 * {@return {@code true} if Sodium Options API is installed, or {@code false} otherwise}
	 */
	public static boolean isSodiumOptionsAPIInstalled() {
		return isSodiumInstalled() && YumiMods.get().isModLoaded("sodiumoptionsapi");
	}
}
