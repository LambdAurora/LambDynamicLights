/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin.lightsource;

import me.lambdaurora.lambdynlights.DynamicLightSource;
import net.minecraft.entity.mob.BlazeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlazeEntity.class)
public abstract class BlazeEntityMixin implements DynamicLightSource
{
    @Shadow
    protected abstract boolean isFireActive();

    private int lambdynlights_luminance;

    @Override
    public void dynamicLightTick()
    {
        if (this.isFireActive()) {
            this.lambdynlights_luminance = 15;
        } else {
            this.lambdynlights_luminance = 10;
        }

        if (!this.isDynamicLightEnabled())
            this.setDynamicLightEnabled(true);
    }

    @Override
    public int getLuminance()
    {
        return this.lambdynlights_luminance;
    }
}
