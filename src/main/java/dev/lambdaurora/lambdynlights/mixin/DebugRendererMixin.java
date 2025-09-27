/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import com.mojang.blaze3d.vertex.MatrixStack;
import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void lambdynlights$onRender(
			MatrixStack matrices, Frustum frustum, MultiBufferSource.BufferSource bufferSource,
			double x, double y, double z,
			CallbackInfo ci
	) {
		var mod = LambDynLights.get();
		for (var debugRenderer : mod.debugRenderers) {
			debugRenderer.render(matrices, bufferSource, x, y, z);
		}
	}
}
