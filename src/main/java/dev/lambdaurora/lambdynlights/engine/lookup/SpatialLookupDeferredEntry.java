/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.lookup;

import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import net.minecraft.core.BlockPos;

/**
 * Represents a deferred dynamic light source entry in a spatial lookup.
 *
 * @param cellKey the cell key of this entry
 * @param behavior the dynamic light source associated with this entry
 * @author LambdAurora, Akarys
 * @version 4.0.0
 * @since 4.0.0
 */
public record SpatialLookupDeferredEntry(int cellKey, DynamicLightBehavior behavior) implements SpatialLookupEntry {
	@Override
	public double getDynamicLightLevel(BlockPos pos) {
		double luminance = this.behavior.lightAtPos(pos, 15. / DynamicLightingEngine.MAX_RADIUS);
		luminance = Math.max(luminance, 0);
		return luminance;
	}
}
