/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.item;

import net.minecraft.world.item.ItemStack;

public interface ItemLightSourceManager {
	/**
	 * {@return the luminance value of the item stack}
	 *
	 * @param stack the item stack
	 */
	default int getLuminance(ItemStack stack) {
		return this.getLuminance(stack, false);
	}

	/**
	 * {@return the luminance value of the item stack}
	 *
	 * @param stack the item stack
	 * @param submergedInWater {@code true} if the stack is submerged in water, else {@code false}
	 */
	int getLuminance(ItemStack stack, boolean submergedInWater);
}
