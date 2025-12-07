/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.resource.entity.luminance;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.lambdaurora.lambdynlights.resource.entity.EntityLightSources;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Range;

import java.util.List;

/**
 * Provides the luminance value of a display entity.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public record DisplayEntityLuminance(List<EntityLuminance> luminance) implements EntityLuminance {
	public static final MapCodec<DisplayEntityLuminance> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					EntityLuminance.LIST_CODEC
							.fieldOf("luminance")
							.forGetter(DisplayEntityLuminance::luminance)
			).apply(instance, DisplayEntityLuminance::new)
	);

	@Override
	public Type type() {
		return EntityLightSources.DISPLAY;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		if (entity instanceof Display display && display.getPackedBrightnessOverride() == -1) {
			return EntityLuminance.getLuminance(itemLightSourceManager, entity, this.luminance);
		}

		return 0;
	}

	/**
	 * Provides the luminance value of a block display entity.
	 */
	public static final class BlockDisplayLuminance implements EntityLuminance {
		public static final BlockDisplayLuminance INSTANCE = new BlockDisplayLuminance();

		private BlockDisplayLuminance() {}

		@Override
		public Type type() {
			return EntityLightSources.BLOCK_DISPLAY;
		}

		@Override
		public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
			if (entity instanceof Display.BlockDisplay display)
				return display.getBlockState().getLightEmission();
			return 0;
		}
	}

	/**
	 * Provides the luminance value of an item display entity.
	 */
	public static final class ItemDisplayLuminance implements EntityLuminance {
		public static final ItemDisplayLuminance INSTANCE = new ItemDisplayLuminance();

		private ItemDisplayLuminance() {}

		@Override
		public Type type() {
			return EntityLightSources.ITEM_DISPLAY;
		}

		@Override
		public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
			if (entity instanceof Display.ItemDisplay display)
				return itemLightSourceManager.getLuminance(display.getItemStack(), display.isUnderWater());
			return 0;
		}
	}
}
