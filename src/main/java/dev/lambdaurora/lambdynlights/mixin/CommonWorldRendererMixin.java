/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.accessor.WorldRendererAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorldRenderer.class, priority = 900)
public abstract class CommonWorldRendererMixin implements WorldRendererAccessor {
	@Invoker("scheduleChunkRender")
	@Override
	public abstract void lambdynlights_scheduleChunkRebuild(int x, int y, int z, boolean important);

	@Inject(
			method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I",
			at = @At("TAIL"),
			cancellable = true
	)
	private static void onGetLightmapCoordinates(BlockRenderView world, BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
		if (!world.getBlockState(pos).isOpaqueFullCube(world, pos) && LambDynLights.get().config.getDynamicLightsMode().isEnabled())
			cir.setReturnValue(LambDynLights.get().getLightmapWithDynamicLight(pos, cir.getReturnValue()));
	}
}
