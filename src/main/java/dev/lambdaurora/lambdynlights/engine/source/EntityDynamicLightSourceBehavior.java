/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.source;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.echo.GuardianEntityLightSource;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents the behavior of a dynamic light source.
 *
 * @author LambdAurora
 * @version 4.4.0
 * @since 4.0.0
 */
@ApiStatus.Internal
public interface EntityDynamicLightSourceBehavior extends EntityDynamicLightSource {
	/**
	 * {@return the dynamic light source previous X-coordinate}
	 */
	double getDynamicLightPrevX();

	/**
	 * {@return the dynamic light source previous Y-coordinate}
	 */
	double getDynamicLightPrevY();

	/**
	 * {@return the dynamic light source previous Z-coordinate}
	 */
	double getDynamicLightPrevZ();

	/**
	 * Updates the previous coordinates of this dynamic light source.
	 */
	void updateDynamicLightPreviousCoordinates();

	void setLuminance(int luminance);

	int getLastDynamicLuminance();

	void setLastDynamicLuminance(int luminance);

	/**
	 * Sets whether the dynamic light is enabled or not.
	 * <p>
	 * Note: please do not call this function in your mod, or you will break things.
	 *
	 * @param enabled {@code true} if the dynamic light is enabled, or {@code false} otherwise
	 */
	@ApiStatus.Internal
	default void setDynamicLightEnabled(boolean enabled) {
		this.resetDynamicLight();
		if (enabled)
			LambDynLights.get().addLightSource(this);
		else
			LambDynLights.get().removeLightSource(this);
	}

	/**
	 * Ticks the given entity for dynamic lighting.
	 *
	 * @param entity the entity to tick
	 */
	static void tickEntity(Entity entity) {
		if (!LambDynLights.get().shouldTick()) return;

		var lightSource = (EntityDynamicLightSourceBehavior) entity;

		if (entity.isRemoved()) {
			lightSource.setDynamicLightEnabled(false);
		} else {
			if (DynamicLightingEngine.canLightUp(entity)) {
				lightSource.dynamicLightTick();
			} else {
				lightSource.setLuminance(0);
			}
			LambDynLights.updateTracking(lightSource);
		}

		if (entity instanceof Guardian guardian) {
			GuardianEntityLightSource.tick(guardian);
		}
	}

	default LongSet getDynamicLightChunksToRebuild(boolean forced) {
		double x = this.getDynamicLightX();
		double y = this.getDynamicLightY();
		double z = this.getDynamicLightZ();
		double deltaX = x - this.getDynamicLightPrevX();
		double deltaY = y - this.getDynamicLightPrevY();
		double deltaZ = z - this.getDynamicLightPrevZ();

		int luminance = this.getLuminance();

		if (!forced && Math.abs(deltaX) <= 0.1 && Math.abs(deltaY) <= 0.1 && Math.abs(deltaZ) <= 0.1 && luminance == this.getLastDynamicLuminance()) {
			return LongSet.of();
		}

		var newPos = new LongOpenHashSet();

		if (luminance > 0) {
			DynamicLightSource.gatherClosestChunks(newPos, x, y, z);
		}

		var result = new LongOpenHashSet(newPos);
		result.addAll(this.lambdynlights$getTrackedLitChunkPos());

		this.updateDynamicLightPreviousCoordinates();
		this.setLastDynamicLuminance(luminance);
		this.lambdynlights$setTrackedLitChunkPos(newPos);

		return result;
	}

	LongSet lambdynlights$getTrackedLitChunkPos();

	void lambdynlights$setTrackedLitChunkPos(LongSet trackedLitChunkPos);
}
