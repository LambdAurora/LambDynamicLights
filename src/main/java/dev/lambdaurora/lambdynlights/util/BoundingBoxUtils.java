/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.util;

import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Contains utilities related to bounding boxes.
 *
 * @author Akarys
 * @version 4.8.0
 * @since 4.8.0
 */
public final class BoundingBoxUtils {
	private BoundingBoxUtils() {
		throw new UnsupportedOperationException("BoundingBoxUtils only contains static definitions.");
	}

	public static <T> Collection<T> forAllPoints(DynamicLightBehavior.BoundingBox box, PointFunction<T> function) {
		var result = new ArrayList<T>(8);

		for (int i = 0; i < 8; i++) {
			result.add(function.apply(
					i >> 2 == 0 ? box.startX() : box.endX(),
					((i >> 1) & 1) == 0 ? box.startY() : box.endY(),
					(i & 1) == 0 ? box.startZ() : box.endZ()
			));
		}

		return result;
	}

	@FunctionalInterface
	public interface PointFunction<T> {
		T apply(int x, int y, int z);
	}
}
