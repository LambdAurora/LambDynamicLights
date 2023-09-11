/*
 * Copyright © 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.sodium;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.util.SodiumDynamicLightHandler;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.model.light.data.ArrayLightDataCache", remap = false)
public abstract class ArrayLightDataCacheMixin {
	@Dynamic
	@Inject(method = "get(III)I", at = @At("HEAD"), require = 0)
	private void lambdynlights$storeLightPos(int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
		if (!LambDynLights.get().config.getDynamicLightsMode().isEnabled())
			return;

		// Store the current light position.
		// This is possible under smooth lighting scenarios, because AoFaceData in Sodium runs a get() call
		// before getting the lightmap.
		SodiumDynamicLightHandler.pos.get().set(x, y, z);
	}
}
