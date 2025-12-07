/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.platform.fabric;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import dev.lambdaurora.lambdynlights.platform.Platform;
import dev.lambdaurora.lambdynlights.resource.LightSourceLoader;
import dev.yumi.commons.event.ListenableEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Provides the Fabric-specific platform operations.
 *
 * @author LambdAurora
 * @version 4.6.0
 * @since 4.5.0
 */
public final class FabricPlatform implements Platform {
	@Override
	public void registerReloader(LightSourceLoader<?> reloader) {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
				.registerReloadListener(new IdentifiableResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return reloader.id();
					}

					@Override
					public Collection<Identifier> getFabricDependencies() {
						return reloader.dependencies();
					}

					@Override
					public CompletableFuture<Void> reload(
							PreparationBarrier synchronizer,
							ResourceManager resourceManager,
							ProfilerFiller prepareProfiler,
							ProfilerFiller applyProfiler,
							Executor prepareExecutor,
							Executor applyExecutor
					) {
						return reloader.reload(
								synchronizer, resourceManager,
								prepareProfiler, applyProfiler,
								prepareExecutor, applyExecutor
						);
					}
				});
	}

	@Override
	public ListenableEvent<Identifier, Consumer<HolderLookup.Provider>> getTagLoadedEvent() {
		return new ListenableEvent<>() {
			@Override
			public Identifier defaultPhaseId() {
				return Event.DEFAULT_PHASE;
			}

			@Override
			public void register(Identifier phaseIdentifier, Consumer<HolderLookup.Provider> listener) {
				CommonLifecycleEvents.TAGS_LOADED.register(
						phaseIdentifier,
						(registries, client) -> {if (client) listener.accept(registries);}
				);
			}

			@Override
			public void addPhaseOrdering(Identifier firstPhase, Identifier secondPhase) {
				CommonLifecycleEvents.TAGS_LOADED.addPhaseOrdering(firstPhase, secondPhase);
			}
		};
	}

	@Override
	public LightSourceLoader.ApplicationPredicate getLightSourceLoaderApplicationPredicate() {
		return (loader, ops, registryAccess, loadedData) -> {
			if (loadedData.data().has(ResourceConditions.CONDITIONS_KEY)) {
				var conditions = ResourceCondition.CONDITION_CODEC.parse(
						ops, loadedData.data().get(ResourceConditions.CONDITIONS_KEY)
				);

				if (conditions.isSuccess()) {
					return conditions.getOrThrow().test(registryAccess);
				} else if (!loadedData.silenceError() || LambDynLightsConstants.FORCE_LOG_ERRORS) {
					conditions.error().ifPresent(error -> LambDynLights.error(loader.getLogger(),
							"Failed to parse Fabric resource conditions for {} light source \"{}\" due to error: {}",
							loader.getResourcePath(), loadedData.id(), error.message()
					));
				}
			}

			return true;
		};
	}
}
