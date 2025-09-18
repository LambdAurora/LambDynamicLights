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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.SonicBoomParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(SonicBoomParticle.class)
public abstract class SonicBoomParticleMixin extends HugeExplosionParticle implements EntityDynamicLightSourceBehavior {
	protected SonicBoomParticleMixin(ClientLevel clientLevel, double d, double e, double f, double g, SpriteSet sprites) {
		super(clientLevel, d, e, f, g, sprites);
	}

	@Override
	public void dynamicLightTick() {
		float factor = 1;

		if (this.age < 4) {
			factor = this.age / 4.f;
		} else if (this.lifetime - this.age < 4) {
			factor = (this.lifetime - this.age) / 4.f;
		}

		this.setLuminance((int) (LightTexture.block(this.getLightColor(0)) * factor * .75f));
	}
}
