/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.item;

import com.google.gson.JsonParser;
import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an item light sources manager.
 *
 * @author LambdAurora
 * @version 2.0.2
 * @since 1.3.0
 */
public final class ItemLightSources {
	private static final JsonParser JSON_PARSER = new JsonParser();
	private static final List<ItemLightSource> ITEM_LIGHT_SOURCES = new ArrayList<>();
	private static final List<ItemLightSource> STATIC_ITEM_LIGHT_SOURCES = new ArrayList<>();

	private ItemLightSources() {
		throw new UnsupportedOperationException("ItemLightSources only contains static definitions.");
	}

	/**
	 * Loads the item light source data from resource pack.
	 *
	 * @param resourceManager The resource manager.
	 */
	public static void load(@NotNull ResourceManager resourceManager) {
		ITEM_LIGHT_SOURCES.clear();

		resourceManager.findResources("dynamiclights/item", path -> path.endsWith(".json")).forEach(id -> load(resourceManager, id));

		ITEM_LIGHT_SOURCES.addAll(STATIC_ITEM_LIGHT_SOURCES);
	}

	private static void load(@NotNull ResourceManager resourceManager, @NotNull Identifier resourceId) {
		var id = new Identifier(resourceId.getNamespace(), resourceId.getPath().replace(".json", ""));
		try {
			var stream = resourceManager.getResource(resourceId).getInputStream();
			var json = JSON_PARSER.parse(new InputStreamReader(stream)).getAsJsonObject();

			ItemLightSource.fromJson(id, json).ifPresent(data -> {
				if (!STATIC_ITEM_LIGHT_SOURCES.contains(data))
					register(data);
			});
		} catch (IOException | IllegalStateException e) {
			LambDynLights.get().warn("Failed to load item light source \"" + id + "\".");
		}
	}

	/**
	 * Registers an item light source data.
	 *
	 * @param data The item light source data.
	 */
	private static void register(@NotNull ItemLightSource data) {
		for (var other : ITEM_LIGHT_SOURCES) {
			if (other.item() == data.item()) {
				LambDynLights.get().warn("Failed to register item light source \"" + data.id() + "\", duplicates item \""
						+ Registry.ITEM.getId(data.item()) + "\" found in \"" + other.id() + "\".");
				return;
			}
		}

		ITEM_LIGHT_SOURCES.add(data);
	}

	/**
	 * Registers an item light source data.
	 *
	 * @param data the item light source data
	 */
	public static void registerItemLightSource(@NotNull ItemLightSource data) {
		for (var other : STATIC_ITEM_LIGHT_SOURCES) {
			if (other.item() == data.item()) {
				LambDynLights.get().warn("Failed to register item light source \"" + data.id() + "\", duplicates item \""
						+ Registry.ITEM.getId(data.item()) + "\" found in \"" + other.id() + "\".");
				return;
			}
		}

		STATIC_ITEM_LIGHT_SOURCES.add(data);
	}

	/**
	 * Returns the luminance of the item in the stack.
	 * Enchantment bonus is calculated here
	 *
	 * @param stack the item stack
	 * @param submergedInWater {@code true} if the stack is submerged in water, else {@code false}
	 * @return a luminance value
	 */
	public static int getLuminance(@NotNull ItemStack stack, boolean submergedInWater) {
		int retval = 0;
		boolean foundSource = false;
		// There is literally negative reasons to iterate through an array rather than use a hashmap here, TODO
		for (var data : ITEM_LIGHT_SOURCES) {
			if (data.item() == stack.getItem()) {
				retval = data.getLuminance(stack, submergedInWater);
				foundSource = true;
				break;
			}
		}
		// If not a registered item type, but it is a block that emits light, find its light level and use that
		if (!foundSource) {
			if (stack.getItem() instanceof BlockItem blockItem) {
				retval = ItemLightSource.BlockItemLightSource.getLuminance(stack, blockItem.getBlock().getDefaultState());
			}
		}
		// Add the bonus light level from an enchantment glint. This is calculated here and not in ItemLightSource because some items (tools, compasses, notch apples, etc) won't be registered but still can have glints
		if (stack.hasGlint()) {
			retval = Math.min(retval + 3, 15);
		}
		return retval;
	}
}
