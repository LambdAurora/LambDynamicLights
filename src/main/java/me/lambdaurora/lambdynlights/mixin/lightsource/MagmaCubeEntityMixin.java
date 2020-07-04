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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MagmaCubeEntity.class)
public abstract class MagmaCubeEntityMixin extends SlimeEntity implements DynamicLightSource
{
    private int lambdynlights_luminance;

    public MagmaCubeEntityMixin(EntityType<? extends SlimeEntity> entityType, World world)
    {
        super(entityType, world);
    }

    @Override
    public void dynamicLightTick()
    {
        if (this.isOnFire()) {
            this.lambdynlights_luminance = 15;
        } else if (this.stretch > 0.6) {
            this.lambdynlights_luminance = 11;
        } else {
            this.lambdynlights_luminance = 8;
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
