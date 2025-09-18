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
import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import dev.lambdaurora.lambdynlights.mixin.RegistryOpsAccessor;
import dev.yumi.commons.Unit;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.io.Resource;
import net.minecraft.resources.io.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
 * @version 4.4.0
 * @since 4.0.0
 */
public abstract class LightSourceLoader<L> implements IdentifiableResourceReloadListener {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambDynamicLights|LightSourceLoader");
	protected static final String SILENCE_ERROR_KEY = "silence_error";

	private final Minecraft client = Minecraft.getInstance();

	protected final List<LoadedLightSourceResource> loadedLightSources = new ArrayList<>();
	protected List<L> lightSources = List.of();

	/**
	 * {@return this light source loader's logger}
	 */
	protected abstract Logger getLogger();

	/**
	 * {@return the path to the light source resources}
	 */
	protected abstract String getResourcePath();

	@Override
	public CompletableFuture<Void> reload(
			Synchronizer synchronizer, ResourceManager resourceManager, Executor prepareExecutor, Executor applyExecutor
	) {
		return CompletableFuture.supplyAsync(() -> {
					this.load(resourceManager);
					return Unit.INSTANCE;
				}, prepareExecutor)
				.thenCompose(synchronizer::whenPrepared)
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

		resourceManager.findResources("dynamiclights/" + this.getResourcePath(), path -> path.path().endsWith(".json"))
				.forEach(this::load);
	}

	/**
	 * Applies the loaded light source data to the given registry state.
	 * <p>
	 * The codecs cannot be fully loaded right on resource load as registry state is not known at this time.
	 *
	 * @param registryAccess the registry access
	 */
	public final void apply(RegistryAccess registryAccess) {
		var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

		var lightSources = this.loadedLightSources.stream()
				.filter(data -> this.canApply(ops, data))
				.map(data -> this.apply(ops, data))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toCollection(ArrayList::new));
		this.doApply(registryAccess, lightSources);
		this.lightSources = lightSources;
	}

	protected void doApply(RegistryAccess registryAccess, List<L> lightSources) {
	}

	protected void load(Identifier resourceId, Resource resource) {
		var id = Identifier.of(resourceId.namespace(), resourceId.path().replace(".json", ""));

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

	protected abstract @NotNull Optional<L> apply(DynamicOps<JsonElement> ops, LoadedLightSourceResource loadedData);

	protected boolean canApply(RegistryOps<JsonElement> ops, LoadedLightSourceResource loadedData) {
		if (loadedData.data().has(ResourceConditions.CONDITIONS_KEY)) {
			var conditions = ResourceCondition.CONDITION_CODEC.parse(
					ops, loadedData.data().get(ResourceConditions.CONDITIONS_KEY)
			);

			var lookupProvider = ((RegistryOpsAccessor) ops).getLookupProvider();

			if (conditions.isSuccess()) {
				return conditions.getOrThrow().test(lookupProvider);
			} else if (!loadedData.silenceError() || LambDynLightsConstants.FORCE_LOG_ERRORS) {
				conditions.error().ifPresent(error ->
						LambDynLights.error(LOGGER,
								"Failed to parse Fabric resource conditions for {} light source \"{}\" due to error: {}",
								this.getResourcePath(), loadedData.id(), error.message()
						)
				);
			}
		}

		return true;
	}
}
