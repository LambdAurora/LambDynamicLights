/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

/**
 * Represents a predicate to match lighting with.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public sealed interface LightSourceLightPredicate {
	Codec<LightSourceLightPredicate> CODEC = Codec.withAlternative(
			Full.CODEC.xmap(full -> full, predicate -> (Full) predicate),
			Any.CODEC
	);

	/**
	 * Matches the light at the given position.
	 *
	 * @param level the level to match in
	 * @param pos the position to match at
	 * @return {@code true} if the light matches, or {@code false} otherwise
	 */
	boolean matches(Level level, BlockPos pos);

	/**
	 * Represents a fully-detailed light predicate.
	 *
	 * @param block the bounds to match of block light
	 * @param sky the bounds to match of skylight
	 */
	record Full(
			MinMaxBounds.Ints block,
			MinMaxBounds.Ints sky
	) implements LightSourceLightPredicate {
		public static final Codec<Full> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				MinMaxBounds.Ints.CODEC.optionalFieldOf("block", MinMaxBounds.Ints.ANY).forGetter(Full::block),
				MinMaxBounds.Ints.CODEC.optionalFieldOf("sky", MinMaxBounds.Ints.ANY).forGetter(Full::sky)
		).apply(instance, Full::new));

		@Override
		public boolean matches(Level level, BlockPos pos) {
			return this.block.matches(level.getBrightness(LightLayer.BLOCK, pos))
					&& this.sky.matches(level.getBrightness(LightLayer.SKY, pos));
		}
	}

	/**
	 * Represents a loose light predicate that matches indiscriminately any light level.
	 *
	 * @param light the bounds to match the light level
	 */
	record Any(MinMaxBounds.Ints light) implements LightSourceLightPredicate {
		public static final Codec<Any> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				MinMaxBounds.Ints.CODEC.optionalFieldOf("any", MinMaxBounds.Ints.ANY).forGetter(Any::light)
		).apply(instance, Any::new));

		@Override
		public boolean matches(Level level, BlockPos pos) {
			return this.light.matches(level.getMaxLocalRawBrightness(pos));
		}
	}
}
