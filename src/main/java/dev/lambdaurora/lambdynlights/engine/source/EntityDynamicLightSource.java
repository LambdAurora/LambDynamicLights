/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.source;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.engine.CellHasher;
import dev.lambdaurora.lambdynlights.engine.lookup.SpatialLookupEntityEntry;
import dev.lambdaurora.lambdynlights.engine.lookup.SpatialLookupEntry;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Range;

import java.util.stream.Stream;

/**
 * Represents an entity-based dynamic light source.
 *
 * @author LambdAurora
 * @version 4.4.0
 * @since 1.0.0
 */
public interface EntityDynamicLightSource extends DynamicLightSource {
	/**
	 * {@return the dynamic light source X-coordinate}
	 */
	double getDynamicLightX();

	/**
	 * {@return the dynamic light source Y-coordinate}
	 */
	double getDynamicLightY();

	/**
	 * {@return the dynamic light source Z-coordinate}
	 */
	double getDynamicLightZ();

	/**
	 * {@return {@code true} if the dynamic light is enabled, or {@code false} otherwise}
	 */
	default boolean isDynamicLightEnabled() {
		return LambDynLights.get().config.getDynamicLightsMode().isEnabled() && LambDynLights.get().containsLightSource(this);
	}

	void resetDynamicLight();

	/**
	 * {@return the luminance of the light source}
	 * The maximum is 15, values below 1 are ignored.
	 */
	@Range(from = 0, to = 15)
	int getLuminance();

	/**
	 * Executed at each tick.
	 */
	void dynamicLightTick();

	@Override
	default Stream<SpatialLookupEntry> splitIntoDynamicLightEntries(CellHasher cellHasher) {
		int x = Mth.floor(this.getDynamicLightX());
		int y = Mth.floor(this.getDynamicLightY());
		int z = Mth.floor(this.getDynamicLightZ());

		int cellKey = cellHasher.hashAt(x, y, z);
		SpatialLookupEntry entry = new SpatialLookupEntityEntry(cellKey, this);

		return Stream.of(entry);
	}
}
