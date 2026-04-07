/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.compat;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.yumi.mc.core.api.YumiMods;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a compatibility layer with another mod.
 *
 * @author LambdAurora
 * @version 4.10.1
 * @since 3.1.4
 */
public interface CompatLayer {
	Logger LOGGER = LoggerFactory.getLogger("LambDynamicLights|CompatLayer");

	/**
	 * Loaded compatibility layers.
	 */
	List<CompatLayer> LAYERS = initLayers();

	/**
	 * Gets the luminance of a living entity from its equipped items.
	 *
	 * @param itemLightSources the item light source manager
	 * @param entity the living entity for which to get the luminance from
	 * @param submergedInWater {@code true} if the entity is submerged in water, or {@code false} otherwise
	 * @return the luminance of a living entity from its equipped items
	 */
	@Range(from = 0, to = 15)
	int getLivingEntityLuminanceFromItems(ItemLightSourceManager itemLightSources, LivingEntity entity, boolean submergedInWater);

	private static List<CompatLayer> initLayers() {
		var layers = new ArrayList<CompatLayer>();

		try {
			if (YumiMods.get().getMod("accessories").isPresent()) {
				layers.add(new AccessoriesCompat());
			} else if (YumiMods.get().getMod("trinkets_updated").isPresent()) {
				layers.add(new TrinketsCompat());
			}
		} catch (LinkageError e) {
			LambDynLights.error(
					LOGGER,
					"Could not load a compatibility layer: THIS IS VERY WRONG, PLEASE REPORT THIS ERROR TO LAMBDYNAMICLIGHTS' AUTHOR ASAP.",
					e
			);
		}

		return layers;
	}
}
