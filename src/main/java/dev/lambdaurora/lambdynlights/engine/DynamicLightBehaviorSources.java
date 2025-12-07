/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehaviorManager;
import dev.lambdaurora.lambdynlights.engine.source.DeferredDynamicLightSource;
import org.jspecify.annotations.Nullable;

/**
 * Represents the dynamic lighting behavior manager implementation.
 *
 * @author LambdAurora, Akarys
 * @version 4.0.1
 * @since 4.0.0
 */
public final class DynamicLightBehaviorSources implements DynamicLightBehaviorManager {
	private final LambDynLights mod;

	public DynamicLightBehaviorSources(LambDynLights mod) {
		this.mod = mod;
	}

	@Override
	public void add(DynamicLightBehavior source) {
		this.mod.addLightSource(new DeferredDynamicLightSource(source));
	}

	@Override
	public boolean remove(@Nullable DynamicLightBehavior source) {
		if (source == null) return false;

		return this.mod.removeLightSources(other -> other instanceof DeferredDynamicLightSource deferred && deferred.behavior() == source);
	}
}
