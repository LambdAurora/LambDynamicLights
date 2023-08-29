/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.ltr;

import dev.lambdaurora.lambdynlights.util.LilTaterBlockEntityAccessor;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "mods.ltr.entities.LilTaterBlockEntity")
public abstract class LilTaterBlockEntityMixin implements Inventory, LilTaterBlockEntityAccessor {
	@Override
	public boolean lambdynlights$isEmpty() {
		return this.isEmpty();
	}

	@Accessor(value = "items", remap = false)
	public abstract DefaultedList<ItemStack> lambdynlights$getItems();
}
