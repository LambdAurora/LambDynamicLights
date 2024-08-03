/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Range;

/**
 * Represents an item light source.
 *
 * @param predicate the predicate to select which items emits the given luminance
 * @param luminance the luminance to emit
 * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
 * @author LambdAurora
 * @version 3.0.0
 * @since 3.0.0
 */
public record ItemLightSource(ItemPredicate predicate, ItemLuminance luminance, boolean waterSensitive) {
	public static final Codec<ItemLightSource> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					ItemPredicate.CODEC.fieldOf("match").forGetter(ItemLightSource::predicate),
					ItemLuminance.CODEC.fieldOf("luminance").forGetter(ItemLightSource::luminance),
					Codec.BOOL.optionalFieldOf("water_sensitive", false).forGetter(ItemLightSource::waterSensitive)
			).apply(instance, ItemLightSource::new)
	);

	public ItemLightSource(ItemPredicate predicate, int luminance) {
		this(predicate, new ItemLuminance.Value(luminance));
	}

	public ItemLightSource(ItemPredicate predicate, ItemLuminance luminance) {
		this(predicate, luminance, false);
	}

	/**
	 * Gets the luminance of the item.
	 *
	 * @param stack the item stack
	 * @return the luminance value between {@code 0} and {@code 15}
	 */
	public @Range(from = 0, to = 15) int getLuminance(ItemStack stack) {
		if (this.predicate.test(stack)) {
			return this.luminance.getLuminance(stack);
		}

		return 0;
	}
}
