/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects the dynamic lighting into the default brightness getter.
 * <p>
 * Injecting into the default brightness getter allows to benefit from brightness caching in the Vanilla renderer.
 *
 * @author LambdAurora
 * @version 4.2.3
 * @since 4.2.3
 */
@Mixin(value = LevelRenderer.BrightnessGetter.class, priority = 900)
public interface BrightnessGetterMixin {
	@Inject(
			method = "method_68890",
			at = @At("TAIL"),
			cancellable = true
	)
	private static void onGetLightmapCoordinates(
			BlockAndTintGetter level, BlockPos pos,
			CallbackInfoReturnable<Integer> cir
	) {
		if (!level.getBlockState(pos).isSolidRender() && LambDynLights.get().config.getDynamicLightsMode().isEnabled())
			cir.setReturnValue(LambDynLights.get().getLightmapWithDynamicLight(level, pos, cir.getReturnValue()));
	}
}
