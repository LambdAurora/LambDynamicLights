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
import dev.lambdaurora.lambdynlights.mixin.RegistryOpsAccessor;
import dev.lambdaurora.lambdynlights.platform.Platform;
import dev.lambdaurora.lambdynlights.platform.PlatformProvider;
import dev.lambdaurora.lambdynlights.resource.LightSourceLoader;
import dev.yumi.commons.event.ListenableEvent;
import dev.yumi.mc.core.api.ModContainer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.resources.io.ResourceType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Provides the Fabric-specific platform operations.
 *
 * @author LambdAurora
 * @version 4.5.0
 * @since 4.5.0
 */
public final class FabricPlatform implements Platform, PlatformProvider {
	@Override
	public Platform getPlatform(ModContainer mod) {
		return this;
	}

	@Override
	public void registerReloader(LightSourceLoader<?> reloader) {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
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
							Synchronizer synchronizer,
							ResourceManager resourceManager,
							Executor prepareExecutor,
							Executor applyExecutor
					) {
						return reloader.reload(synchronizer, resourceManager, prepareExecutor, applyExecutor);
					}
				});
	}

	@Override
	public ListenableEvent<Identifier, Consumer<RegistryAccess>> getTagLoadedEvent() {
		return new ListenableEvent<>() {
			@Override
			public @NotNull Identifier defaultPhaseId() {
				return Event.DEFAULT_PHASE;
			}

			@Override
			public void register(@NotNull Identifier phaseIdentifier, @NotNull Consumer<RegistryAccess> listener) {
				CommonLifecycleEvents.TAGS_LOADED.register(
						phaseIdentifier,
						(registries, client) -> {if (client) listener.accept(registries);}
				);
			}

			@Override
			public void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
				CommonLifecycleEvents.TAGS_LOADED.addPhaseOrdering(firstPhase, secondPhase);
			}
		};
	}

	@Override
	public LightSourceLoader.ApplicationPredicate getLightSourceLoaderApplicationPredicate() {
		return (loader, ops, loadedData) -> {
			if (loadedData.data().has(ResourceConditions.CONDITIONS_KEY)) {
				var conditions = ResourceCondition.CONDITION_CODEC.parse(
						ops, loadedData.data().get(ResourceConditions.CONDITIONS_KEY)
				);

				var lookupProvider = ((RegistryOpsAccessor) ops).getLookupProvider();

				if (conditions.isSuccess()) {
					return conditions.getOrThrow().test(lookupProvider);
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
