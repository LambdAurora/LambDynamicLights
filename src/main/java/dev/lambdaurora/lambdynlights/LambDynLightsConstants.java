/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Contains constants about LambDynamicLights.
 *
 * @author LambdAurora
 * @version 3.0.1
 * @since 3.0.1
 */
public final class LambDynLightsConstants {
	/**
	 * The namespace of this mod, whose value is {@value}.
	 */
	public static final String NAMESPACE = "lambdynlights";

	/**
	 * {@return {@code true} if this mod is in development mode, or {@code false} otherwise}
	 */
	public static boolean isDevMode() {
		return FabricLoader.getInstance().getModContainer(NAMESPACE)
				.map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString().endsWith("-local"))
				.orElse(true);
	}
}
