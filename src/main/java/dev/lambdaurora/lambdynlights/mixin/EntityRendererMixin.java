/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {
	@ModifyReturnValue(method = "getBlockLightLevel", at = @At("RETURN"))
	private int onGetBlockLight(int original, T entity, BlockPos pos) {
		if (!LambDynLights.get().config.getDynamicLightsMode().isEnabled())
			return original;

		int entityLuminance = ((EntityDynamicLightSource) entity).getLuminance();
		if (entityLuminance >= 15)
			return entityLuminance;

		int posLuminance = (int) LambDynLights.get().getDynamicLightLevel(pos);

		return Math.max(Math.max(original, entityLuminance), posLuminance);
	}
}
