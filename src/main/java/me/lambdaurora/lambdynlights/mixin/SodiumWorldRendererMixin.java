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
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Represents a compatibility mixin for Sodium.
 * <p>
 * Instead of injecting in {@link WorldRenderer#setupTerrain(Camera, Frustum, boolean, int, boolean)} we inject before the setupTerrain call in
 * {@link WorldRenderer#render(MatrixStack, float, long, boolean, Camera, GameRenderer, LightmapTextureManager, Matrix4f)}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Mixin(WorldRenderer.class)
public class SodiumWorldRendererMixin
{
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZIZ)V",
                    shift = At.Shift.BEFORE
            )
    )
    public void onSetupTerrain(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci)
    {
        LambDynLights.get().updateAll((WorldRenderer) (Object) this);
    }
}
