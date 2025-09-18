/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSourceBehavior;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a dynamic light source.
 *
 * @author LambdAurora
 * @version 3.0.0
 * @since 1.0.0
 *
 * @deprecated This is fully removed in LambDynamicLights releases targeting Minecraft 1.21.4 and newer.
 * <p>
 * There is no direct replacement, but depending on your use case please read the official documentation:
 * <ul>
 *     <li><a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/entity.html">Entity Lighting</a></li>
 *     <li><a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/java.html">Modding Interfaces</a></li>
 * </ul>
 */
@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "4.0.0+1.21.4")
public interface DynamicLightSource extends EntityDynamicLightSourceBehavior {
	/**
	 * Returns the dynamic light source world.
	 *
	 * @return the world instance
	 */
	Level getDynamicLightLevel();

	default boolean lambdynlights$updateDynamicLight(@NotNull LevelRenderer renderer) {
		return false;
	}

	default void lambdynlights$scheduleTrackedChunksRebuild(@NotNull LevelRenderer renderer) {}
}
