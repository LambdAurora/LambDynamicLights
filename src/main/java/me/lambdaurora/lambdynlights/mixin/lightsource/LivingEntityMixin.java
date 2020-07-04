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
import me.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements DynamicLightSource
{
    private int lambdynlights_luminance;

    public LivingEntityMixin(EntityType<?> type, World world)
    {
        super(type, world);
    }

    @Override
    public void dynamicLightTick()
    {
        if (this.isOnFire()) {
            this.lambdynlights_luminance = 15;
        } else {
            int luminance = 0;
            for (ItemStack equipped : this.getItemsEquipped()) {
                luminance = Math.max(luminance, LambDynLights.getLuminanceFromItemStack(equipped));
            }

            this.lambdynlights_luminance = luminance;
        }
    }

    @Override
    public int getLuminance()
    {
        return this.lambdynlights_luminance;
    }
}
