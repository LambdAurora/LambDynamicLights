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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.jetbrains.annotations.Nullable;
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

    @Shadow @Nullable public abstract BlockEntity getBlockEntity(BlockPos pos);

    @Inject(
            method = "tickBlockEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V", shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void onBlockEntityTick(CallbackInfo ci, Profiler profiler, Iterator<BlockEntity> iterator, BlockEntityTickInvoker blockEntity)
    {
        if (this.isClient() && LambDynLights.get().config.hasBlockEntitiesLightSource() && this.getBlockEntity(blockEntity.getPos()) != null) {
           ((DynamicLightSource) this.getBlockEntity(blockEntity.getPos())).dynamicLightTick();
        }
    }
}
