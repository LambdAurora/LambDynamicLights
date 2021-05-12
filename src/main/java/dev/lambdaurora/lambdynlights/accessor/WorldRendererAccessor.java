/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.accessor;

/**
 * Represents an accessor for WorldRenderer.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public interface WorldRendererAccessor {
    /**
     * Schedules a chunk rebuild.
     *
     * @param x X coordinates of the chunk.
     * @param y Y coordinates of the chunk.
     * @param z Z coordinates of the chunk.
     * @param important True if important, else false.
     */
    void lambdynlights_scheduleChunkRebuild(int x, int y, int z, boolean important);
}
