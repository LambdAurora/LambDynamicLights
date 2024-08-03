/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.resource.item;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSource;
import net.minecraft.resources.Identifier;
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
public final class ItemLightSources {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambDynLights|ItemLightSources");
	private static final List<ItemLightSource> LIGHT_SOURCES = new ArrayList<>();

	private ItemLightSources() {
		throw new UnsupportedOperationException("ItemLightSources only contains static definitions.");
	}

	/**
	 * Loads the item light source data from resource pack.
	 *
	 * @param resourceManager The resource manager.
	 */
	public static void load(ResourceManager resourceManager) {
		LIGHT_SOURCES.clear();

		resourceManager.findResources("dynamiclights/item", path -> path.path().endsWith(".json"))
				.forEach(ItemLightSources::load);
	}

	private static void load(Identifier resourceId, Resource resource) {
		var id = Identifier.of(resourceId.namespace(), resourceId.path().replace(".json", ""));
		try (var reader = new InputStreamReader(resource.open())) {
			var json = JsonParser.parseReader(reader).getAsJsonObject();

			var loaded = ItemLightSource.CODEC.parse(JsonOps.INSTANCE, json);

			loaded.ifError(error -> {
				LOGGER.warn("[LambDynamicLights] Failed to load item light source \"{}\" due to error: {}", id, error.message());
			});
			loaded.ifSuccess(LIGHT_SOURCES::add);
		} catch (IOException | IllegalStateException e) {
			LOGGER.warn("[LambDynamicLights] Failed to load item light source \"{}\".", id, e);
		}
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
	 *
	public static void registerItemLightSource(ItemLightSource data) {
	var other = STATIC_ITEM_LIGHT_SOURCES.get(data.item());

	if (other != null) {
	LambDynLights.get().warn("Failed to register item light source \"" + data.id() + "\", duplicates item \""
	+ BuiltInRegistries.ITEM.getId(data.item()) + "\" found in \"" + other.id() + "\".");
	return;
	}

	STATIC_ITEM_LIGHT_SOURCES.put(data.item(), data);
	}*/

	/**
	 * Returns the luminance of the item in the stack.
	 *
	 * @param stack the item stack
	 * @param submergedInWater {@code true} if the stack is submerged in water, else {@code false}
	 * @return a luminance value
	 */
	public static int getLuminance(ItemStack stack, boolean submergedInWater) {
		boolean shouldCareAboutWater = submergedInWater && LambDynLights.get().config.getWaterSensitiveCheck().get();

		int luminance = Block.byItem(stack.getItem()).defaultState().getLightEmission();

		for (var data : LIGHT_SOURCES) {
			if (shouldCareAboutWater && data.waterSensitive()) continue;

			luminance = Math.max(luminance, data.getLuminance(stack));
		}

		return luminance;
	}
}
