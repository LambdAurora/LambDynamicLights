/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin;

import me.lambdaurora.lambdynlights.LambDynLights;
import me.lambdaurora.lambdynlights.LambDynLightsCompat;
import me.lambdaurora.lambdynlights.accessor.WorldRendererAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorldRenderer.class, priority = 900)
public abstract class CommonWorldRendererMixin implements WorldRendererAccessor
{
    @Shadow
    @Final
    private MinecraftClient client;

    @Invoker("scheduleChunkRender")
    @Override
    public abstract void lambdynlights_scheduleChunkRebuild(int x, int y, int z, boolean important);

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void onRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci)
    {
        if (LambDynLightsCompat.isSodiumInstalled() || LambDynLightsCompat.isCanvasInstalled()) {
            this.client.getProfiler().swap("dynamic_lighting");
            LambDynLights.get().updateAll((WorldRenderer) (Object) this);
        }
    }

    @Inject(
            method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I",
            at = @At("TAIL"),
            cancellable = true
    )
    private static void onGetLightmapCoordinates(BlockRenderView world, BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> cir)
    {
        if (!world.getBlockState(pos).isOpaqueFullCube(world, pos) && LambDynLights.get().config.getDynamicLightsMode().isEnabled())
            cir.setReturnValue(LambDynLights.get().getLightmapWithDynamicLight(pos, cir.getReturnValue()));
    }
}
