/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.lightsource;

import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSourceBehavior;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireflyParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FireflyParticle.class)
public abstract class FireflyParticleMixin extends TextureSheetParticle implements EntityDynamicLightSourceBehavior {
	@Shadow
	public abstract int getLightColor(float tickDelta);

	protected FireflyParticleMixin(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
	}

	@Override
	public void dynamicLightTick() {
		this.setLuminance(LightTexture.block(this.getLightColor(0)) / 2);
	}
}
