/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.lookup;

import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSource;
import net.minecraft.core.BlockPos;

/**
 * Represents an entity dynamic light source entry in a spatial lookup.
 *
 * @param cellKey the cell key of this entry
 * @param source the dynamic light source associated with this entry
 * @author LambdAurora, Akarys
 * @version 4.0.0
 * @since 4.0.0
 */
public record SpatialLookupEntityEntry(int cellKey, EntityDynamicLightSource source) implements SpatialLookupEntry {
	@Override
	public double getDynamicLightLevel(BlockPos pos) {
		int luminance = this.source.getLuminance();
		if (luminance > 0) {
			return SpatialLookupEntry.lightAtPos(
					this.source.getDynamicLightX(),
					this.source.getDynamicLightY(),
					this.source.getDynamicLightZ(),
					pos,
					luminance
			);
		}
		return 0.;
	}
}
