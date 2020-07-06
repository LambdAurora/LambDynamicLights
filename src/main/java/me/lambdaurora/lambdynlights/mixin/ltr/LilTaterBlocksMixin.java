/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin.ltr;

import me.lambdaurora.lambdynlights.LambDynLights;
import me.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "mods.ltr.registry.LilTaterBlocks")
public class LilTaterBlocksMixin
{
    @Accessor("LIL_TATER_BLOCK_ENTITY")
    private static BlockEntityType<?> getLilTaterBlockEntity()
    {
        throw new IllegalStateException("Accessor did not apply properly.");
    }

    @Inject(method = "init", at = @At("TAIL"))
    private static void onInit(CallbackInfo ci)
    {
        DynamicLightHandlers.registerDynamicLightHandler(getLilTaterBlockEntity(), entity -> {
            int luminance = 0;
            if (!((LilTaterBlockEntityAccessor) entity).lambdynlights_isEmpty()) {
                for (ItemStack item : ((LilTaterBlockEntityAccessor) entity).lambdynlights_getItems()) {
                    luminance = Math.max(luminance, LambDynLights.getLuminanceFromItemStack(item));
                }
            }
            return luminance;
        });
    }
}
