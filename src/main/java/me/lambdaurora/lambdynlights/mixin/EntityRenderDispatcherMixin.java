/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin;

import me.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin
{
    @Inject(method = "getLight", at = @At("RETURN"), cancellable = true)
    public void getLight(Entity entity, float tickDelta, CallbackInfoReturnable<Integer> cir)
    {
        if (!LambDynLights.get().config.getDynamicLightsMode().isEnabled())
            return; // Do not touch to the value.
        int vanilla = cir.getReturnValue();
        cir.setReturnValue(LambDynLights.get().getLightmapWithDynamicLight(entity, vanilla));
    }
}
