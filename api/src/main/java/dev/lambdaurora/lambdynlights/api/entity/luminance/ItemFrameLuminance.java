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
import net.minecraft.world.entity.decoration.ItemFrame;
import org.jetbrains.annotations.Range;

/**
 * Provides the luminance value of an item frame.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public final class ItemFrameLuminance implements EntityLuminance {
	public static final ItemFrameLuminance INSTANCE = new ItemFrameLuminance();

	private ItemFrameLuminance() {}

	@Override
	public Type type() {
		return Type.ITEM_FRAME;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		if (entity instanceof ItemFrame itemFrame) {
			var world = entity.level();
			return itemLightSourceManager.getLuminance(itemFrame.getItem(), !world.getFluidState(entity.blockPosition()).isEmpty());
		}

		return 0;
	}
}
