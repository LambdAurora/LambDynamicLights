/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a dynamic light source.
 */
public interface DynamicLightSource
{
    /**
     * Returns the entity the dynamic light source is associated with.
     *
     * @return The associated entity.
     */
    Entity getDynamicLightEntity();

    /**
     * Returns whether the dynamic light is enabled or not.
     *
     * @return True if the dynamic light is enabled, else false.
     */
    default boolean isDynamicLightEnabled()
    {
        return LambDynLights.get().config.getDynamicLightsMode().isEnabled() && LambDynLights.get().containsLightSource(this);
    }

    /**
     * Sets whether the dynamic light is enabled or not.
     * <p>
     * Note: please do not call this function in your mod or you will break things.
     *
     * @param enabled True if the dynamic light is enabled, else false.
     */
    default void setDynamicLightEnabled(boolean enabled)
    {
        if (enabled)
            LambDynLights.get().addLightSource(this);
        else
            LambDynLights.get().removeLightSource(this);
    }

    /**
     * Returns the luminance of the light source.
     * The maximum is 15, below 1 values are ignored.
     *
     * @return The luminance of the light source.
     */
    default int getLuminance()
    {
        return this.getDynamicLightEntity().isOnFire() ? 15 : 0;
    }

    /**
     * Executed at each tick.
     */
    default void dynamicLightTick()
    {
    }

    /**
     * Returns whether this dynamic light source should update.
     *
     * @return True if this dynamic light source should update, else false.
     */
    boolean shouldUpdateDynamicLight();

    void lambdynlights_updateDynamicLight(@NotNull WorldRenderer renderer);

    void lambdynlights_scheduleTrackedChunksRebuild(@NotNull WorldRenderer renderer);
}
