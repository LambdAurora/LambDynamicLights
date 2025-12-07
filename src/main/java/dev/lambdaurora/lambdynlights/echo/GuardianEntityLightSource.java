/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.echo;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.behavior.LineLightBehavior;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import net.minecraft.world.entity.monster.Guardian;
import org.jetbrains.annotations.Contract;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

/**
 * Represents a Guardian laser light source.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public interface GuardianEntityLightSource {
	@Contract(pure = true)
	@Nullable LineLightBehavior lambdynlights$getDynamicLightBeam();

	void lambdynlights$setDynamicLightBeam(@Nullable LineLightBehavior beam);

	static void tick(Guardian guardian) {
		var lightSource = (GuardianEntityLightSource) guardian;
		var target = guardian.getActiveAttackTarget();

		if (guardian.isRemoved() || !LambDynLights.get().config.getGuardianLaser().get()) {
			LambDynLights.get().dynamicLightBehaviorManager().remove(lightSource.lambdynlights$getDynamicLightBeam());
			lightSource.lambdynlights$setDynamicLightBeam(null);
			return;
		}

		if (!DynamicLightingEngine.canLightUp(guardian)) {
			LambDynLights.get().dynamicLightBehaviorManager().remove(lightSource.lambdynlights$getDynamicLightBeam());
			lightSource.lambdynlights$setDynamicLightBeam(null);
			return;
		}

		if (target != null) {
			LineLightBehavior beam;

			if (lightSource.lambdynlights$getDynamicLightBeam() == null) {
				beam = new LineLightBehavior(
						new Vector3d(guardian.getX(), guardian.getY(), guardian.getZ()),
						new Vector3d(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ()),
						7
				) {
					@Override
					public boolean isRemoved() {
						return guardian.isRemoved();
					}
				};
				lightSource.lambdynlights$setDynamicLightBeam(beam);
				LambDynLights.get().dynamicLightBehaviorManager().add(beam);
			} else {
				beam = lightSource.lambdynlights$getDynamicLightBeam();
				beam.setStartPoint(guardian.getX(), guardian.getY(), guardian.getZ());
				beam.setEndPoint(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
			}

			int luminance = 7;

			if (guardian.getClientSideAttackTime() >= guardian.getAttackDuration() - 5) {
				luminance = 13;
			} else {
				luminance += (int) (guardian.getAttackAnimationScale(0.f) * 5);
			}

			beam.setLuminance(luminance);
		} else if (lightSource.lambdynlights$getDynamicLightBeam() != null) {
			LambDynLights.get().dynamicLightBehaviorManager().remove(lightSource.lambdynlights$getDynamicLightBeam());
			lightSource.lambdynlights$setDynamicLightBeam(null);
		}
	}
}
