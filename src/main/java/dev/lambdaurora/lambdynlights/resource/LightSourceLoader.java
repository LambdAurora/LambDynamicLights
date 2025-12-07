/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.yumi.commons.Unit;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Represents a light source loader.
 *
 * @param <L> the type of light source to load
 * @author LambdAurora
 * @version 4.6.0
 * @since 4.0.0
 */
public abstract class LightSourceLoader<L> implements PreparableReloadListener {
	protected static final String SILENCE_ERROR_KEY = "silence_error";

	private final Minecraft client = Minecraft.getInstance();
	private final ApplicationPredicate applicationPredicate;

	protected final List<LoadedLightSourceResource> loadedLightSources = new ArrayList<>();
	protected List<L> lightSources = List.of();

	protected LightSourceLoader(ApplicationPredicate applicationPredicate) {
		this.applicationPredicate = applicationPredicate;
	}

	/**
	 * {@return the identifier of this resource reloader}
	 */
	public abstract Identifier id();

	/**
	 * {@return the dependencies of this resource reloader}
	 */
	public abstract @Unmodifiable Collection<Identifier> dependencies();

	/**
	 * {@return this light source loader's logger}
	 */
	public abstract Logger getLogger();

	/**
	 * {@return the path to the light source resources}
	 */
	public abstract String getResourcePath();

	@Override
	public String getName() {
		return this.id().toString();
	}

	@Override
	public CompletableFuture<Void> reload(
			PreparationBarrier synchronizer, ResourceManager resourceManager,
			ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler,
			Executor prepareExecutor, Executor applyExecutor
	) {
		return CompletableFuture.supplyAsync(() -> {
					this.load(resourceManager);
					return Unit.INSTANCE;
				}, prepareExecutor)
				.thenCompose(synchronizer::wait)
				.thenAcceptAsync((reloadState) -> {
					if (this.client.level != null) {
						this.apply(this.client.level.registryAccess());
					}
				}, applyExecutor);
	}

	/**
	 * Loads the light source data from resource packs.
	 *
	 * @param resourceManager the resource manager
	 */
	protected void load(ResourceManager resourceManager) {
		this.loadedLightSources.clear();

		resourceManager.listResources("dynamiclights/" + this.getResourcePath(), path -> path.getPath().endsWith(".json"))
				.forEach(this::load);
	}

	/**
	 * Applies the loaded light source data to the given registry state.
	 * <p>
	 * The codecs cannot be fully loaded right on resource load as registry state is not known at this time.
	 *
	 * @param registryLookup the registry access
	 */
	public final void apply(HolderLookup.Provider registryLookup) {
		var ops = RegistryOps.create(JsonOps.INSTANCE, registryLookup);

		var lightSources = this.loadedLightSources.stream()
				.filter(data -> this.canApply(ops, registryLookup, data))
				.map(data -> this.apply(ops, data))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toCollection(ArrayList::new));
		this.doApply(registryLookup, lightSources);
		this.lightSources = lightSources;
	}

	protected void doApply(HolderLookup.Provider registryLookup, List<L> lightSources) {
	}

	protected void load(Identifier resourceId, Resource resource) {
		var id = Identifier.fromNamespaceAndPath(resourceId.getNamespace(), resourceId.getPath().replace(".json", ""));

		try (var reader = new InputStreamReader(resource.open())) {
			var rawJson = JsonParser.parseReader(reader);

			if (!rawJson.isJsonObject()) {
				LambDynLights.warn(
						this.getLogger(),
						"Failed to load {} light source \"{}\". Expected JSON object in file.",
						this.getResourcePath(), id
				);
				return;
			}

			var json = rawJson.getAsJsonObject();
			boolean silentError = false;

			if (json.has(SILENCE_ERROR_KEY)) {
				silentError = json.get(SILENCE_ERROR_KEY).getAsBoolean();
				json.remove(SILENCE_ERROR_KEY);
			}

			this.loadedLightSources.add(new LoadedLightSourceResource(id, json, silentError));
		} catch (IOException | IllegalStateException e) {
			LambDynLights.warn(this.getLogger(), "Failed to load {} light source \"{}\".", this.getResourcePath(), id, e);
		}
	}

	protected abstract Optional<L> apply(DynamicOps<JsonElement> ops, LoadedLightSourceResource loadedData);

	protected boolean canApply(RegistryOps<JsonElement> ops, HolderLookup.Provider registryAccess, LoadedLightSourceResource loadedData) {
		return this.applicationPredicate.canApply(this, ops, registryAccess, loadedData);
	}

	@FunctionalInterface
	public interface ApplicationPredicate {
		boolean canApply(
				LightSourceLoader<?> loader,
				RegistryOps<JsonElement> ops,
				HolderLookup.Provider registryAccess,
				LoadedLightSourceResource loadedData
		);

		class Pending implements ApplicationPredicate {
			private @Nullable ApplicationPredicate wrapped;

			public void set(@Nullable ApplicationPredicate wrapped) {
				this.wrapped = wrapped;
			}

			@Override
			public boolean canApply(
					LightSourceLoader<?> loader,
					RegistryOps<JsonElement> ops,
					HolderLookup.Provider registryAccess,
					LoadedLightSourceResource loadedData
			) {
				return this.wrapped == null || this.wrapped.canApply(loader, ops, registryAccess, loadedData);
			}
		}
	}
}
