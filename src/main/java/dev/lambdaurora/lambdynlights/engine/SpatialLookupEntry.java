/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine;

import dev.lambdaurora.lambdynlights.DynamicLightSource;

/**
 * Represents an entry in a spatial lookup.
 *
 * @param cellKey the cell key of this entry
 * @param source the dynamic light source associated with this entry
 * @author LambdAurora, Akarys
 * @version 3.1.0
 * @since 3.1.0
 */
public record SpatialLookupEntry(int cellKey, DynamicLightSource source) {
}
