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


	int getLuminance(ItemStack stack);

	int getLuminance(ItemStack stack, boolean submergedInWater);
}
