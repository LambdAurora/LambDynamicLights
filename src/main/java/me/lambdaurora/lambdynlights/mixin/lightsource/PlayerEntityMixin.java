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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DynamicLightSource
{
    @Shadow
    public abstract boolean isSpectator();

    private int lambdynlights_luminance;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world)
    {
        super(entityType, world);
    }

    @Override
    public void dynamicLightTick()
    {
        if (this.isOnFire()) {
            this.lambdynlights_luminance = 15;
        } else {
            int luminance = 0;
            for (ItemStack equipped : this.getItemsEquipped()) {
                luminance = Math.max(luminance, LambDynLights.getLuminanceFromItemStack(equipped, this.isSubmergedInWater()));
            }

            this.lambdynlights_luminance = luminance;
        }

        if (this.isSpectator())
            this.lambdynlights_luminance = 0;
    }

    @Override
    public int getLuminance()
    {
        return this.lambdynlights_luminance;
    }
}
