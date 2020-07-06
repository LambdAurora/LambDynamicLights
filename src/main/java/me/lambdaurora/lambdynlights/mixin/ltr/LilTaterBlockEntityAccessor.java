/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin.ltr;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "mods.ltr.entities.LilTaterBlockEntity")
public interface LilTaterBlockEntityAccessor
{
    @Invoker("isEmpty")
    boolean lambdynlights_isEmpty();

    @Accessor("items")
    DefaultedList<ItemStack> lambdynlights_getItems();
}
