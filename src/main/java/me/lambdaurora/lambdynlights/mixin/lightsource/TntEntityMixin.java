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
import me.lambdaurora.lambdynlights.ExplosiveLightingMode;
import me.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity implements DynamicLightSource
{
    @Shadow
    private int fuseTimer;

    private double lambdynlights_startFuseTimer = 80.0;
    private int    lambdynlights_luminance;

    public TntEntityMixin(EntityType<?> type, World world)
    {
        super(type, world);
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    private void onNew(EntityType<? extends TntEntity> entityType, World world, CallbackInfo ci)
    {
        this.lambdynlights_startFuseTimer = this.fuseTimer;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci)
    {
        if (this.getEntityWorld().isClient()) {
            if (!LambDynLights.get().config.getTntLightingMode().isEnabled())
                return;

            // We do not want to update the entity on the server.
            if (this.removed) {
                this.setDynamicLightEnabled(false);
            } else {
                this.dynamicLightTick();
                LambDynLights.updateTracking(this);
            }
        }
    }

    @Override
    public void dynamicLightTick()
    {
        if (this.isOnFire()) {
            this.lambdynlights_luminance = 15;
        } else {
            ExplosiveLightingMode lightingMode = LambDynLights.get().config.getTntLightingMode();
            if (lightingMode == ExplosiveLightingMode.FANCY) {
                double fuse = this.fuseTimer / this.lambdynlights_startFuseTimer;
                this.lambdynlights_luminance = (int) (-(fuse * fuse) * 10.0) + 10;
            } else {
                this.lambdynlights_luminance = 10;
            }
        }
    }

    @Override
    public int getLuminance()
    {
        return this.lambdynlights_luminance;
    }
}
