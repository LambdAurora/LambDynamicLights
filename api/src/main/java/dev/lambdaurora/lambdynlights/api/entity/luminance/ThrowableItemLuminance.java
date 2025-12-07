/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.entity.luminance;

import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import org.jetbrains.annotations.Range;

/**
 * Provides the luminance value derived from the item of a throwable item projectile.
 *
 * @author LambdAurora
 * @version 4.0.2
 * @since 4.0.2
 */
public final class ThrowableItemLuminance implements EntityLuminance {
	public static final ThrowableItemLuminance INSTANCE = new ThrowableItemLuminance();

	private ThrowableItemLuminance() {}

	@Override
	public Type type() {
		return Type.THROWABLE_ITEM;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		if (entity instanceof ThrowableItemProjectile tip) {
			return itemLightSourceManager.getLuminance(tip.getItem(), tip.isUnderWater());
		}

		return 0;
	}
}
