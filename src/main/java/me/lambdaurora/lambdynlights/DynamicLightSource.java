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
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a dynamic light source.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public interface DynamicLightSource
{
    /**
     * Returns the dynamic light source X coordinate.
     *
     * @return The X coordinate.
     */
    double getDynamicLightX();

    /**
     * Returns the dynamic light source Y coordinate.
     *
     * @return The Y coordinate.
     */
    double getDynamicLightY();

    /**
     * Returns the dynamic light source Z coordinate.
     *
     * @return The Z coordinate.
     */
    double getDynamicLightZ();

    /**
     * Returns the dynamic light source world.
     *
     * @return The world instance.
     */
    World getDynamicLightWorld();

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
        this.resetDynamicLight();
        if (enabled)
            LambDynLights.get().addLightSource(this);
        else
            LambDynLights.get().removeLightSource(this);
    }

    void resetDynamicLight();

    /**
     * Returns the luminance of the light source.
     * The maximum is 15, below 1 values are ignored.
     *
     * @return The luminance of the light source.
     */
    int getLuminance();

    /**
     * Executed at each tick.
     */
    void dynamicLightTick();

    /**
     * Returns whether this dynamic light source should update.
     *
     * @return True if this dynamic light source should update, else false.
     */
    boolean shouldUpdateDynamicLight();

    void lambdynlights_updateDynamicLight(@NotNull WorldRenderer renderer);

    void lambdynlights_scheduleTrackedChunksRebuild(@NotNull WorldRenderer renderer);
}
