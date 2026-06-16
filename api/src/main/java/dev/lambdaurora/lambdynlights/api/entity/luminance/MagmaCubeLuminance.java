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
import org.jetbrains.annotations.Range;

/**
 * Provides the luminance value of a magma cube.
 *
 * @author LambdAurora
 * @version 4.11.0
 * @since 4.1.0
 * @deprecated Prefer {@link CubeMobSquishLuminance} instead.
 */
@Deprecated
public final class MagmaCubeLuminance implements EntityLuminance {
	public static final MagmaCubeLuminance INSTANCE = new MagmaCubeLuminance();
	private final CubeMobSquishLuminance actual = new CubeMobSquishLuminance(
			EntityLuminance.of(8),
			EntityLuminance.of(11),
			0.6f
	);

	private MagmaCubeLuminance() {}

	@Override
	public Type type() {
		return EntityLuminance.Type.MAGMA_CUBE;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		return this.actual.getLuminance(itemLightSourceManager, entity);
	}
}
