/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.behavior;

import org.jspecify.annotations.Nullable;

/**
 * Represents the dynamic lighting behavior manager,
 * which provides the ability to add and remove custom dynamic lighting sources.
 *
 * @author LambdAurora, Akarys
 * @version 4.0.0
 * @see DynamicLightBehavior
 * @since 4.0.0
 */
public interface DynamicLightBehaviorManager {
	/**
	 * Adds the given dynamic lighting source to the client world.
	 *
	 * @param source the source to add
	 */
	void add(DynamicLightBehavior source);

	/**
	 * Removes the given dynamic lighting source from the client world.
	 *
	 * @param source the source to remove
	 * @return {@code true} if the dynamic lighting source was present and removed, or {@code false} otherwise
	 */
	boolean remove(@Nullable DynamicLightBehavior source);
}
