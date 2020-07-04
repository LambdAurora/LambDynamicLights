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
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(EndermanEntity.class)
public abstract class EndermanEntityMixin extends HostileEntity implements DynamicLightSource
{
    @Shadow
    @Nullable
    public abstract BlockState getCarriedBlock();

    private int lambdynlights_luminance;

    protected EndermanEntityMixin(EntityType<? extends HostileEntity> entityType, World world)
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
                luminance = Math.max(luminance, LambDynLights.getLuminanceFromItemStack(equipped));
            }

            BlockState carriedBlock = this.getCarriedBlock();
            if (carriedBlock != null) {
                luminance = Math.max(luminance, carriedBlock.getLuminance());
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
