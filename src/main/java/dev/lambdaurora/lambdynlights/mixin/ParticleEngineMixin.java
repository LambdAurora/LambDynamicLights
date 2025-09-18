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
import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSourceBehavior;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	@Inject(
			method = "tickParticle",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;tick()V", shift = At.Shift.AFTER)
	)
	private void lambdynlights$onTick(Particle particle, CallbackInfo ci) {
		var lightSource = (EntityDynamicLightSourceBehavior) particle;

		if (!particle.isAlive()) {
			lightSource.setDynamicLightEnabled(false);
		} else {
			if (LambDynLights.get().canLightParticle(particle)) {
				lightSource.dynamicLightTick();
			} else {
				lightSource.setLuminance(0);
			}
			LambDynLights.updateTracking(lightSource);
		}
	}
}
