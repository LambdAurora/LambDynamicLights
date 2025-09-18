/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to MinecraftClient.
 * <p>
 * Goal: clear light sources cache when changing world.
 *
 * @author LambdAurora
 * @version 4.4.0
 * @since 1.3.2
 */
@Mixin(Minecraft.class)
public class MinecraftClientMixin {
	@Inject(method = "tick", at = @At("RETURN"))
	private void lambdynlights$onEndTick(CallbackInfo info) {
		LambDynLights.get().onEndClientTick((Minecraft) (Object) this);
	}

	@Inject(method = "updateLevelInEngines", at = @At("HEAD"))
	private void onUpdateLevelInEngines(ClientLevel level, CallbackInfo ci) {
		LambDynLights.get().onChangeWorld();
	}
}
