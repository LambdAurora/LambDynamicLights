/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin;

import me.lambdaurora.lambdynlights.DynamicLightSource;
import me.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(World.class)
public abstract class WorldMixin
{
    @Shadow
    public abstract boolean isClient();

    @Inject(
            method = "tickBlockEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;isRemoved()Z", shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void onBlockEntityTick(CallbackInfo ci, Profiler profiler, Iterator<BlockEntity> iterator, BlockEntity blockEntity)
    {
        if (this.isClient() && LambDynLights.get().config.hasBlockEntitiesLightSource()) {
            ((DynamicLightSource) blockEntity).dynamicLightTick();
        }
    }
}
