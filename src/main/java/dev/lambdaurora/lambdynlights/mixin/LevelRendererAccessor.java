/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Represents an accessor for WorldRenderer.
 *
 * @author LambdAurora
 * @version 4.2.3
 * @since 1.0.0
 */
@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	/**
	 * Schedules a chunk rebuild.
	 *
	 * @param x X coordinates of the chunk
	 * @param y Y coordinates of the chunk
	 * @param z Z coordinates of the chunk
	 * @param important {@code true} if important, else {@code false}
	 */
	@Invoker("setSectionDirty")
	void lambdynlights$scheduleChunkRebuild(int x, int y, int z, boolean important);
}
