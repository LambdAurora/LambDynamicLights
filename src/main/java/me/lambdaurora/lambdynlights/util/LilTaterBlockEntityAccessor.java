/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

public interface LilTaterBlockEntityAccessor
{
    boolean lambdynlights_isEmpty();

    DefaultedList<ItemStack> lambdynlights_getItems();
}
