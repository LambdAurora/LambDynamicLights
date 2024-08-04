/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.resource.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSource;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.yumi.commons.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.io.Resource;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an item light sources manager.
 *
 * @author LambdAurora
 * @version 3.0.0
 * @since 1.3.0
 */
public final class ItemLightSources implements ItemLightSourceManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambDynamicLights|ItemLightSources");
	private static final String SILENCE_ERROR_KEY = "silence_error";
	private static final boolean FORCE_LOG_ERRORS = TriState.fromProperty("lambdynamiclights.resource.force_log_errors")
			.toBooleanOrElse(FabricLoader.getInstance().isDevelopmentEnvironment());

	private final List<LoadedItemLightSource> loadedLightSources = new ArrayList<>();
	private final List<ItemLightSource> lightSources = new ArrayList<>();

	/**
	 * Loads the item light source data from resource pack.
	 *
	 * @param resourceManager The resource manager.
	 */
	public void load(ResourceManager resourceManager) {
		this.loadedLightSources.clear();

		resourceManager.findResources("dynamiclights/item", path -> path.path().endsWith(".json"))
				.forEach(this::load);
	}

	/**
	 * Applies the loaded item light source data to the given registry state.
	 * <p>
	 * The codecs cannot be fully loaded right on resource load as registry state is not known at this time.
	 *
	 * @param registryAccess the registry access
	 */
	public void apply(RegistryAccess registryAccess) {
		var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

		this.lightSources.clear();
		this.loadedLightSources.forEach(data -> apply(ops, data));
	}

	private void load(Identifier resourceId, Resource resource) {
		var id = Identifier.of(resourceId.namespace(), resourceId.path().replace(".json", ""));

		try (var reader = new InputStreamReader(resource.open())) {
			var rawJson = JsonParser.parseReader(reader);

			if (!rawJson.isJsonObject()) {
				LambDynLights.warn(LOGGER, "Failed to load item light source \"{}\". Expected JSON object in file.", id);
				return;
			}

			var json = rawJson.getAsJsonObject();
			boolean silentError = false;

			if (json.has(SILENCE_ERROR_KEY)) {
				silentError = json.get(SILENCE_ERROR_KEY).getAsBoolean();
				json.remove(SILENCE_ERROR_KEY);
			}

			this.loadedLightSources.add(new LoadedItemLightSource(id, json, silentError));
		} catch (IOException | IllegalStateException e) {
			LambDynLights.warn(LOGGER, "Failed to load item light source \"{}\".", id, e);
		}
	}

	private void apply(DynamicOps<JsonElement> ops, LoadedItemLightSource loadedData) {
		var loaded = ItemLightSource.CODEC.parse(ops, loadedData.data());

		if (!loadedData.silenceError() || FORCE_LOG_ERRORS) {
			// Some files may choose to silence errors, especially if it's expected for some data to not always be present.
			// This should be used rarely to avoid issues.
			// Errors may be forced to be logged if the property "lambdynamiclights.resource.force_log_errors" is true
			// or if the environment is a development environment.
			loaded.ifError(error -> {
				LambDynLights.warn(LOGGER, "Failed to load item light source \"{}\" due to error: {}", loadedData.id(), error.message());
			});
		}
		loaded.ifSuccess(this.lightSources::add);
	}

	/**
	 * Registers an item light source data.
	 *
	 * @param data The item light source data.
	 *
	private static void register(ItemLightSource data) {
	var other = ITEM_LIGHT_SOURCES.get(data.item());

	if (other != null) {
	LambDynLights.get().warn("Failed to register item light source \"" + data.id() + "\", duplicates item \""
	+ BuiltInRegistries.ITEM.getId(data.item()) + "\" found in \"" + other.id() + "\".");
	return;
	}

	ITEM_LIGHT_SOURCES.put(data.item(), data);
	}*/

	/**
	 * Registers an item light source data.
	 *
	 * @param data the item light source data
	 * <p>
	 * public static void registerItemLightSource(ItemLightSource data) {
	 * var other = STATIC_ITEM_LIGHT_SOURCES.get(data.item());
	 * <p>
	 * if (other != null) {
	 * LambDynLights.get().warn("Failed to register item light source \"" + data.id() + "\", duplicates item \""
	 * + BuiltInRegistries.ITEM.getId(data.item()) + "\" found in \"" + other.id() + "\".");
	 * return;
	 * }
	 * <p>
	 * STATIC_ITEM_LIGHT_SOURCES.put(data.item(), data);
	 * }
	 */

	@Override
	public int getLuminance(ItemStack stack, boolean submergedInWater) {
		boolean shouldCareAboutWater = submergedInWater && LambDynLights.get().config.getWaterSensitiveCheck().get();

		int luminance = Block.byItem(stack.getItem()).defaultState().getLightEmission();

		for (var data : lightSources) {
			if (shouldCareAboutWater && data.waterSensitive()) continue;

			luminance = Math.max(luminance, data.getLuminance(stack));
		}

		return luminance;
	}

	/**
	 * Represents a partially loaded item light source awaiting full load once registries are known.
	 *
	 * @param id the identifier of the item light source
	 * @param data the data to fully load
	 * @param silenceError {@code true} if load errors should be silenced, or {@code false} otherwise
	 */
	record LoadedItemLightSource(Identifier id, JsonObject data, boolean silenceError) {}
}
