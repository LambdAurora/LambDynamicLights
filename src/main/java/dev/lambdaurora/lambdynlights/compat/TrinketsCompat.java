/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.compat;

import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.LivingEntity;

/**
 * Represents the Trinkets compatibility layer.
 *
 * @author LambdAurora
 * @version 4.10.1
 * @since 3.1.4
 */
final class TrinketsCompat implements CompatLayer {
	@Override
	public int getLivingEntityLuminanceFromItems(ItemLightSourceManager itemLightSources, LivingEntity entity, boolean submergedInWater) {
		int luminance = 0;
		var component = TrinketsApi.getAttachment(entity);

		for (var equipped : component.getAllEquipped()) {
			if (!equipped.getB().isEmpty()) {
				luminance = Math.max(luminance, itemLightSources.getLuminance(equipped.getB(), submergedInWater));

				if (luminance >= 15) {
					break;
				}
			}
		}

		return luminance;
	}
}
