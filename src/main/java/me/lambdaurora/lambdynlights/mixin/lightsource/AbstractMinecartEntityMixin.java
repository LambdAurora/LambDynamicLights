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
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the tick method for dynamic light source tracking in minecart entities.
 *
 * @author LambdAurora
 * @version 1.3.2
 * @since 1.3.2
 */
@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin extends Entity implements DynamicLightSource
{
    public AbstractMinecartEntityMixin(EntityType<?> type, World world)
    {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci)
    {
        // We do not want to update the entity on the server.
        if (this.getEntityWorld().isClient()) {
            if (this.isRemoved()) {
                this.setDynamicLightEnabled(false);
            } else {
                this.dynamicLightTick();
                LambDynLights.updateTracking(this);
            }
        }
    }
}
