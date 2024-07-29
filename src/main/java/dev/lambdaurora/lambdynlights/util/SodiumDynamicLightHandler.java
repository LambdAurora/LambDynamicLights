/*
 * Copyright © 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.util;

import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface SodiumDynamicLightHandler {
	// Stores the current light position being used by ArrayLightDataCache#get
	// We use ThreadLocal because Sodium's chunk builder is multithreaded, otherwise it will break
	// catastrophically.
	ThreadLocal<BlockPos.Mutable> pos = ThreadLocal.withInitial(BlockPos.Mutable::new);

	static int getLightmap(BlockPos pos, int word, int lightmap) {
		if (!LambDynLights.get().config.getDynamicLightsMode().isEnabled())
			return lightmap;

		// Equivalent to world.getBlockState(pos).isOpaqueFullCube(world, pos)
		if (/*LightDataAccess.unpackFO(word)*/ (word >>> 30 & 1) != 0)
			return lightmap;

		double dynamic = LambDynLights.get().getDynamicLightLevel(pos);
		return LambDynLights.get().getLightmapWithDynamicLight(dynamic, lightmap);
	}
}
