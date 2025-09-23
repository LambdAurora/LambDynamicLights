/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.source;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.yumi.mc.core.api.YumiMods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;

import java.util.function.Consumer;

/**
 * Represents the ticking behavior of particle light sources.
 *
 * @author LambdAurora
 * @version 4.4.2
 * @since 4.4.2
 */
public final class ParticleLightSourceBehavior {
	private static final Consumer<Particle> DO_TICK_PARTICLE;

	public static void tickParticle(Particle particle) {
		DO_TICK_PARTICLE.accept(particle);
	}

	private static void doTickParticle(Particle particle) {
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

	static {
		if (YumiMods.get().isModLoaded("asyncparticles")) {
			DO_TICK_PARTICLE = particle -> Minecraft.getInstance().execute(() -> doTickParticle(particle));
		} else {
			DO_TICK_PARTICLE = ParticleLightSourceBehavior::doTickParticle;
		}
	}
}
