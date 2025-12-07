/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
	@Shadow
	@Final
	private List<DebugRenderer.SimpleDebugRenderer> opaqueRenderers;

	@Shadow
	@Final
	private List<DebugRenderer.SimpleDebugRenderer> translucentRenderers;

	@Inject(method = "refreshRendererList", at = @At("TAIL"))
	private void lambdynlights$onRender(CallbackInfo ci) {
		var mod = LambDynLights.get();
		this.opaqueRenderers.addAll(mod.opaqueDebugRenderers);
		this.translucentRenderers.addAll(mod.transparentDebugRenderers);
	}
}
