/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api;

import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;

/**
 * Represents the entrypoint for LambDynamicLights API.
 *
 * @author LambdAurora
 * @version 3.0.0
 * @since 1.3.2
 */
public interface DynamicLightsInitializer {
	/**
	 * Called when LambDynamicLights is initialized to register custom dynamic light handlers and item light sources.
	 */
	void onInitializeDynamicLights(ItemLightSourceManager itemLightSourceManager);
}
