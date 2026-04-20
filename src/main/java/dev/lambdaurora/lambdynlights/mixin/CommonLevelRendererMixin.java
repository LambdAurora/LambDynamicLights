/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
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
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class CommonLevelRendererMixin {
	@ModifyReturnValue(
			method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
			at = @At("RETURN")
	)
	private static int onGetLightmapCoordinates(int original, BlockAndTintGetter level, BlockState state, BlockPos pos) {
		if (!level.getBlockState(pos).isSolidRender(level, pos) && LambDynLights.get().config.getDynamicLightsMode().isEnabled())
			return LambDynLights.get().getLightmapWithDynamicLight(level, pos, original);

		return original;
	}
}
