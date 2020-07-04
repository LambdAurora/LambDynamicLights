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
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends HostileEntity implements DynamicLightSource
{
    @Shadow
    public abstract boolean getIgnited();

    @Shadow
    public abstract float getClientFuseTime(float timeDelta);

    private int lambdynlights_luminance;

    protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world)
    {
        super(entityType, world);
    }

    @Override
    public void dynamicLightTick()
    {
        if (this.isOnFire()) {
            this.lambdynlights_luminance = 15;
        } else if (this.getIgnited() && this.getClientFuseTime(0.0F) > 0.001D) {
            this.lambdynlights_luminance = 10;
        } else {
            this.lambdynlights_luminance = 0;
        }
    }

    @Override
    public int getLuminance()
    {
        return this.lambdynlights_luminance;
    }
}
