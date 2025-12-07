/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.resource.entity.luminance;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.lambdaurora.lambdynlights.resource.entity.EntityLightSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import org.jetbrains.annotations.Range;

/**
 * Provides the luminance value of a creeper.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public final class CreeperLuminance implements EntityLuminance {
	public static final CreeperLuminance INSTANCE = new CreeperLuminance();

	private CreeperLuminance() {}

	@Override
	public Type type() {
		return EntityLightSources.CREEPER;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		int luminance = 0;

		if (entity instanceof Creeper creeper) {
			if (creeper.getSwelling(0.f) > 0.001) {
				luminance = switch (LambDynLights.get().config.getCreeperLightingMode()) {
					case OFF -> 0;
					case SIMPLE -> 10;
					case FANCY -> (int) (creeper.getSwelling(0.f) * 10.0);
				};
			}
		}

		return luminance;
	}
}
