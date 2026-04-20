/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Injects the dynamic lighting into the default brightness getter.
 * <p>
 * Injecting into the default brightness getter allows to benefit from brightness caching in the Vanilla renderer.
 *
 * @author LambdAurora
 * @version 4.10.2
 * @since 4.2.3
 */
@Mixin(value = LevelRenderer.BrightnessGetter.class, priority = 900)
public interface BrightnessGetterMixin {
	@ModifyReturnValue(
			method = "lambda$static$0",
			at = @At("RETURN"),
			remap = false,
			allow = 1,
			require = 1
	)
	private static int onGetLightmapCoordinates(
			int original,
			BlockAndLightGetter level, BlockPos pos
	) {
		if (!level.getBlockState(pos).isSolidRender() && LambDynLights.get().config.getDynamicLightsMode().isEnabled())
			return LambDynLights.get().getLightmapWithDynamicLight(level, pos, original);

		return original;
	}
}
