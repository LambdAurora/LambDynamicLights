/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.lightsource;

import dev.lambdaurora.lambdynlights.DynamicLightSource;
import dev.lambdaurora.lambdynlights.ExplosiveLightingMode;
import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity implements DynamicLightSource {
    @Shadow
    public abstract int getFuse();

    @Unique
    private int startFuseTimer = 80;
    @Unique
    private int luminance;

    public TntEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    private void onNew(EntityType<? extends TntEntity> entityType, World world, CallbackInfo ci) {
        this.startFuseTimer = this.getFuse();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        // We do not want to update the entity on the server.
        if (this.getEntityWorld().isClient()) {
            if (!LambDynLights.get().config.getTntLightingMode().isEnabled())
                return;

            if (this.isRemoved()) {
                this.setDynamicLightEnabled(false);
            } else {
                this.dynamicLightTick();
                LambDynLights.updateTracking(this);
            }
        }
    }

    @Override
    public void dynamicLightTick() {
        if (this.isOnFire()) {
            this.luminance = 15;
        } else {
            ExplosiveLightingMode lightingMode = LambDynLights.get().config.getTntLightingMode();
            if (lightingMode == ExplosiveLightingMode.FANCY) {
                var fuse = this.getFuse() / this.startFuseTimer;
                this.luminance = (int) (-(fuse * fuse) * 10.0) + 10;
            } else {
                this.luminance = 10;
            }
        }
    }

    @Override
    public int getLuminance() {
        return this.luminance;
    }
}
