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
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Range;

/**
 * Provides the luminance value of an item entity.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public final class ItemEntityLuminance implements EntityLuminance {
	public static final ItemEntityLuminance INSTANCE = new ItemEntityLuminance();

	private ItemEntityLuminance() {}

	@Override
	public Type type() {
		return Type.ITEM_ENTITY;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		if (entity instanceof ItemEntity itemEntity) {
			return itemLightSourceManager.getLuminance(itemEntity.getItem(), entity.isUnderWater());
		}

		return 0;
	}
}
