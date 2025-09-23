/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.platform;

import dev.lambdaurora.lambdynlights.resource.LightSourceLoader;
import dev.yumi.commons.event.ListenableEvent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

/**
 * Represents platform-specific operations to implement on said platforms.
 *
 * @author LambdAurora
 * @version 4.5.0
 * @since 4.5.0
 */
public interface Platform {
	void registerReloader(LightSourceLoader<?> reloader);

	ListenableEvent<Identifier, Consumer<RegistryAccess>> getTagLoadedEvent();

	LightSourceLoader.ApplicationPredicate getLightSourceLoaderApplicationPredicate();
}
