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
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.jetbrains.annotations.Range;

/**
 * Provides the luminance value derived from the item of an arrow projectile.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public final class ArrowItemDerivedLuminance implements EntityLuminance {
	public static final ArrowItemDerivedLuminance INSTANCE = new ArrowItemDerivedLuminance();

	private ArrowItemDerivedLuminance() {}

	@Override
	public Type type() {
		return Type.ARROW_ITEM_DERIVED;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		if (entity instanceof AbstractArrow arrow) {
			return itemLightSourceManager.getLuminance(arrow.getPickupItemStackOrigin(), entity.isUnderWater());
		}

		return 0;
	}
}
