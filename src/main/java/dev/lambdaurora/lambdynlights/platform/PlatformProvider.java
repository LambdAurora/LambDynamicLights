/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.platform;

import dev.yumi.mc.core.api.ModContainer;

/**
 * Represents the platform provider given the loading mod.
 *
 * @author LambdAurora
 * @version 4.5.0
 * @since 4.5.0
 */
public interface PlatformProvider {
	Platform getPlatform(ModContainer mod);
}
