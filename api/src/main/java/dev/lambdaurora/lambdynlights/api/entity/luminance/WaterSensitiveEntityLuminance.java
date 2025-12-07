/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.entity.luminance;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a conditional luminance value depending on whether the entity is in or out of water.
 *
 * @param outOfWater the luminance values if the entity is out of water
 * @param inWater the luminance values if the entity is in water
 * @author LambdAurora
 * @version 4.1.0
 * @since 4.1.0
 */
public record WaterSensitiveEntityLuminance(
		List<EntityLuminance> outOfWater,
		List<EntityLuminance> inWater
) implements EntityLuminance {
	public static final MapCodec<WaterSensitiveEntityLuminance> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					EntityLuminance.LIST_CODEC
							.optionalFieldOf("out_of_water", List.of())
							.forGetter(WaterSensitiveEntityLuminance::outOfWater),
					EntityLuminance.LIST_CODEC
							.optionalFieldOf("in_water", List.of())
							.forGetter(WaterSensitiveEntityLuminance::inWater)
			).apply(instance, WaterSensitiveEntityLuminance::new)
	);

	@Override
	public Type type() {
		return EntityLuminance.Type.WATER_SENSITIVE;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		boolean submergedInWater = entity.isUnderWater();

		if (submergedInWater) {
			return EntityLuminance.getLuminance(itemLightSourceManager, entity, this.inWater);
		} else {
			return EntityLuminance.getLuminance(itemLightSourceManager, entity, this.outOfWater);
		}
	}

	/**
	 * Creates a new {@link WaterSensitiveEntityLuminance} builder.
	 *
	 * @return the builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Represents a builder for creating new {@link WaterSensitiveEntityLuminance} instances.
	 */
	public static class Builder {
		private final List<EntityLuminance> outOfWater = new ArrayList<>();
		private final List<EntityLuminance> inWater = new ArrayList<>();

		/**
		 * Adds the given luminance values to use in the case the entity is out of water.
		 *
		 * @param luminances the luminance values if the entity is out of water
		 * @return {@code this}
		 */
		@Contract("_ -> this")
		public Builder outOfWater(EntityLuminance... luminances) {
			this.outOfWater.addAll(List.of(luminances));
			return this;
		}

		/**
		 * Adds the given luminance values to use in the case the entity is out of water.
		 *
		 * @param luminances the luminance values if the entity is out of water
		 * @return {@code this}
		 */
		@Contract("_ -> this")
		public Builder outOfWater(List<EntityLuminance> luminances) {
			this.outOfWater.addAll(luminances);
			return this;
		}

		/**
		 * Adds the given luminance values to use in the case the entity is in water.
		 *
		 * @param luminances the luminance values if the entity is in water
		 * @return {@code this}
		 */
		@Contract("_ -> this")
		public Builder inWater(EntityLuminance... luminances) {
			this.inWater.addAll(List.of(luminances));
			return this;
		}

		/**
		 * Adds the given luminance values to use in the case the entity is in water.
		 *
		 * @param luminances the luminance values if the entity is in water
		 * @return {@code this}
		 */
		@Contract("_ -> this")
		public Builder inWater(List<EntityLuminance> luminances) {
			this.inWater.addAll(luminances);
			return this;
		}

		/**
		 * Builds the resulting {@link WaterSensitiveEntityLuminance}.
		 *
		 * @return the resulting {@link WaterSensitiveEntityLuminance}
		 */
		public WaterSensitiveEntityLuminance build() {
			return new WaterSensitiveEntityLuminance(List.copyOf(this.outOfWater), List.copyOf(this.inWater));
		}
	}
}
