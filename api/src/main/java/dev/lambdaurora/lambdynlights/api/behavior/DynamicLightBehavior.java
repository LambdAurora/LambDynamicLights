/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.behavior;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Range;

/**
 * Represents a dynamic lighting behavior for custom light sources.
 * <p>
 * Each dynamic lighting behavior have a way to give a light level at a given position, a bounding box, and a way to check for changes.
 *
 * @author LambdAurora, Akarys
 * @version 4.0.1
 * @since 4.0.0
 */
public interface DynamicLightBehavior {
	/**
	 * Returns the light level at the given position.
	 * <p>
	 * While lights in Minecraft normally light up to 15 blocks of distance, LambDynamicLights has a cap on how far away
	 * a source may interact with the world. The argument {@code fallofRatio} is used to convert between the usual Minecraft
	 * distance scale to the one expected by the mod. Most commonly, you'll be multiplying your computed distance by this ratio.
	 *
	 * <h3>Example - a single point emitting light</h3>
	 * {@snippet lang = "java":
	 * public @Range(from = 0, to = 15) double lightAtPos(BlockPos pos, double falloffRatio) {
	 * 	double dx = pos.getX() - this.x + 0.5;
	 * 	double dy = pos.getY() - this.y + 0.5;
	 * 	double dz = pos.getZ() - this.z + 0.5;
	 *
	 * 	double distanceSquared = dx * dx + dy * dy + dz * dz;
	 * 	return Math.max(luminance - Math.sqrt(distanceSquared) * falloffRatio, 0.0);
	 * }
	 *}
	 *
	 * @param pos the position of the light query
	 * @param falloffRatio the rate at which the light level should fall to {@code 0} the furthest it is from the source
	 * @return a light level at the given position, between {@code 0} and {@code 15}
	 */
	@Range(from = 0, to = 15)
	double lightAtPos(BlockPos pos, double falloffRatio);

	/**
	 * {@return the bounding box of the actively light-emitting volume}
	 * This must not include falloff light.
	 *
	 * <h3>Example - a single point emitting light</h3>
	 * {@snippet lang = "java":
	 * public BoundingBox getBoundingBox() {
	 * 	// Doesn't depend on the luminance of the source since falloff isn't included
	 * 	return new BoundingBox(this.x, this.y, this.z, this.x + 1, this.y + 1, this.z + 1);
	 * }
	 *}
	 */
	BoundingBox getBoundingBox();

	/**
	 * {@return {@code true} if this dynamic lighting source state has changed since the last time this function was called,
	 * or {@code false} otherwise}
	 */
	boolean hasChanged();

	/**
	 * {@return {@code true} if this dynamic lighting source has been removed, or {@code false} otherwise}
	 * By default, dynamic lighting behavior must be removed explicitly (as-in this returns {@code false}).
	 * This method exists for cases in which the removal of the source is not a set event and can only be known for sure through polling its state.
	 */
	default boolean isRemoved() {
		return false;
	}

	/**
	 * Represents the bounding box of a dynamic lighting behavior.
	 *
	 * @param startX the starting X-coordinate of this bounding box
	 * @param startY the starting Y-coordinate of this bounding box
	 * @param startZ the starting Z-coordinate of this bounding box
	 * @param endX the ending X-coordinate of this bounding box
	 * @param endY the ending Y-coordinate of this bounding box
	 * @param endZ the ending Z-coordinate of this bounding box
	 */
	record BoundingBox(int startX, int startY, int startZ, int endX, int endY, int endZ) {
		public BoundingBox(
				int startX,
				int startY,
				int startZ,
				int endX,
				int endY,
				int endZ
		) {
			this.startX = Math.min(startX, endX);
			this.startY = Math.min(startY, endY);
			this.startZ = Math.min(startZ, endZ);
			this.endX = Math.max(startX, endX);
			this.endY = Math.max(startY, endY);
			this.endZ = Math.max(startZ, endZ);
		}
	}
}
