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
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SpectralArrowEntity.class)
public abstract class SpectralArrowEntityMixin extends PersistentProjectileEntity implements DynamicLightSource
{
    protected SpectralArrowEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world)
    {
        super(entityType, world);
    }

    @Override
    public void dynamicLightTick()
    {
        if (!this.isDynamicLightEnabled())
            this.setDynamicLightEnabled(true);
    }

    @Override
    public int getLuminance()
    {
        return this.isOnFire() ? 15 : 8;
    }
}
