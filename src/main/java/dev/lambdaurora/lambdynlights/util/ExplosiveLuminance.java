/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.util;

import dev.lambdaurora.lambdynlights.ExplosiveLightingMode;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSourceBehavior;
import net.minecraft.util.Mth;

import java.util.function.IntSupplier;

/**
 * Provides explosive luminance ticking logic.
 *
 * @author LambdAurora
 * @version 4.12.0
 * @see dev.lambdaurora.lambdynlights.mixin.lightsource.PrimedTntEntityMixin
 * @see dev.lambdaurora.lambdynlights.mixin.lightsource.SulfurCubeMixin
 * @since 4.12.0
 */
public final class ExplosiveLuminance {
	private ExplosiveLuminance() {}

	public static void doExplosiveLuminanceTick(
			EntityDynamicLightSourceBehavior lightSource,
			IntSupplier fuseGetter,
			IntSupplier startFuseTimerGetter,
			Runnable tickRunner
	) {
		if (!LambDynLights.get().config.getTntLightingMode().isEnabled()) {
			lightSource.setLuminance(0);
			return;
		}

		tickRunner.run();

		ExplosiveLightingMode lightingMode = LambDynLights.get().config.getTntLightingMode();
		int luminance;
		if (lightingMode == ExplosiveLightingMode.FANCY) {
			luminance = (int) (-Mth.smoothstep((float) fuseGetter.getAsInt() / startFuseTimerGetter.getAsInt()) * 10.f) + 10;
		} else {
			luminance = 10;
		}

		lightSource.setLuminance(Math.max(lightSource.getLuminance(), luminance));
	}
}
