/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.accessor;

import net.minecraft.client.renderer.culling.Frustum;

public interface FrustumStorage {
	Frustum lambdynlights$getFrustum();
}
