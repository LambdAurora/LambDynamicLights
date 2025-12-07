/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.entity.luminance;

import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.jetbrains.annotations.Range;

/**
 * Provides the luminance value derived from a falling block's luminance.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public final class FallingBlockLuminance implements EntityLuminance {
	public static final FallingBlockLuminance INSTANCE = new FallingBlockLuminance();

	private FallingBlockLuminance() {}

	@Override
	public Type type() {
		return Type.FALLING_BLOCK;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		if (entity instanceof FallingBlockEntity fallingBlock) {
			return fallingBlock.getBlockState().getLightEmission();
		}

		return 0;
	}
}
