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
import net.minecraft.world.entity.monster.EnderMan;
import org.jetbrains.annotations.Range;

/**
 * Provides the luminance value of an enderman.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public final class EndermanLuminance implements EntityLuminance {
	public static final EndermanLuminance INSTANCE = new EndermanLuminance();

	private EndermanLuminance() {}

	@Override
	public Type type() {
		return Type.ENDERMAN;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		if (entity instanceof EnderMan enderman && enderman.getCarriedBlock() != null) {
			return enderman.getCarriedBlock().getLightEmission();
		}

		return 0;
	}
}
