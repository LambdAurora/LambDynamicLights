/*
 * Copyright © 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.fabric;

import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator", remap = false)
public abstract class AoCalculatorMixin {
	@Dynamic
	@Inject(method = "getLightmapCoordinates", at = @At(value = "RETURN", ordinal = 0), require = 0, cancellable = true, remap = false)
	private static void onGetLightmapCoordinates(BlockRenderView world, BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
		if (!world.getBlockState(pos).isOpaqueFullCube(world, pos) && LambDynLights.get().config.getDynamicLightsMode().isEnabled())
			cir.setReturnValue(LambDynLights.get().getLightmapWithDynamicLight(pos, cir.getReturnValue()));
	}
}
