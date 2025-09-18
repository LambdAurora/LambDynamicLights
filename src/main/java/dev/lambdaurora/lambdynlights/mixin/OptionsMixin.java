/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import dev.lambdaurora.lambdynlights.LambDynLights;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Options.class)
public class OptionsMixin {
	@Mutable
	@Shadow
	@Final
	public KeyMapping[] keyMappings;

	@Inject(at = @At("HEAD"), method = "load()V")
	private void lambdynlights$onLoad(CallbackInfo ci) {
		var keyMappings = new KeyMapping[this.keyMappings.length + 1];
		System.arraycopy(this.keyMappings, 0, keyMappings, 0, this.keyMappings.length);
		keyMappings[keyMappings.length - 1] = LambDynLights.TOGGLE_FPS_DYNAMIC_LIGHTING;
		this.keyMappings = keyMappings;
	}
}
