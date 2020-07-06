/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.api;

import me.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a dynamic light handler.
 *
 * @param <T> The type of the light source.
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.1.0
 */
public interface DynamicLightHandler<T>
{
    /**
     * Returns the luminance of the light source.
     *
     * @param lightSource The light source.
     * @return The luminance.
     */
    int getLuminance(T lightSource);

    /**
     * Returns a living entity dynamic light handler.
     *
     * @param handler The handler.
     * @param <T>     The type of the entity.
     * @return The completed handler.
     */
    static <T extends LivingEntity> @NotNull DynamicLightHandler<T> makeLivingEntityHandler(@NotNull DynamicLightHandler<T> handler)
    {
        return entity -> {
            int luminance = 0;
            for (ItemStack equipped : entity.getItemsEquipped()) {
                luminance = Math.max(luminance, LambDynLights.getLuminanceFromItemStack(equipped));
            }
            return Math.max(luminance, handler.getLuminance(entity));
        };
    }
}
