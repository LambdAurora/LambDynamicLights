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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;

import java.util.Optional;

/**
 * Represents a predicate to match a location with.
 * <p>
 * This is inspired from the {@linkplain net.minecraft.advancements.criterion.LocationPredicate location predicate}
 * found in advancements but with fewer features since this one needs to work on the client.
 *
 * @param position the position predicate to match if present
 * @param biomes the biomes to match if present
 * @param dimension the dimension to match if present
 * @param smokey {@code true} to match if the given position is smokey, or {@code false} otherwise
 * @param light the light predicate to match if present
 * @param canSeeSky {@code true} to match if the given position can see the sky, or {@code false} otherwise
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
public record LightSourceLocationPredicate(
		Optional<PositionPredicate> position,
		Optional<HolderSet<Biome>> biomes,
		Optional<ResourceKey<Level>> dimension,
		Optional<Boolean> smokey,
		Optional<LightSourceLightPredicate> light,
		Optional<Boolean> canSeeSky
) {
	public static final Codec<LightSourceLocationPredicate> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							PositionPredicate.CODEC.optionalFieldOf("position").forGetter(LightSourceLocationPredicate::position),
							RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes").forGetter(LightSourceLocationPredicate::biomes),
							ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(LightSourceLocationPredicate::dimension),
							Codec.BOOL.optionalFieldOf("smokey").forGetter(LightSourceLocationPredicate::smokey),
							LightSourceLightPredicate.CODEC.optionalFieldOf("light").forGetter(LightSourceLocationPredicate::light),
							Codec.BOOL.optionalFieldOf("can_see_sky").forGetter(LightSourceLocationPredicate::canSeeSky)
					)
					.apply(instance, LightSourceLocationPredicate::new)
	);

	public boolean matches(Level level, double x, double y, double z) {
		if (this.position.isPresent() && !this.position.get().matches(x, y, z)) {
			return false;
		} else if (this.dimension.isPresent() && this.dimension.get() != level.dimension()) {
			return false;
		} else {
			BlockPos pos = BlockPos.containing(x, y, z);
			boolean loaded = level.isLoaded(pos);
			if (this.biomes.isEmpty() || loaded && this.biomes.get().contains(level.getBiome(pos))) {
				if (this.smokey.isEmpty() || loaded && this.smokey.get() == CampfireBlock.isSmokeyPos(level, pos)) {
					if (this.light.isPresent() && !this.light.get().matches(level, pos)) {
						return false;
					} else {
						return this.canSeeSky.isEmpty() || this.canSeeSky.get() == level.canSeeSky(pos);
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	public static class Builder {
		private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
		private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
		private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
		private Optional<HolderSet<Biome>> biomes = Optional.empty();
		private Optional<ResourceKey<Level>> dimension = Optional.empty();
		private Optional<Boolean> smokey = Optional.empty();
		private Optional<LightSourceLightPredicate> light = Optional.empty();
		private Optional<Boolean> canSeeSky = Optional.empty();

		public static Builder location() {
			return new Builder();
		}

		public static Builder inBiome(Holder<Biome> holder) {
			return location().biomes(HolderSet.direct(holder));
		}

		public static Builder inDimension(ResourceKey<Level> resourceKey) {
			return location().dimension(resourceKey);
		}

		public static Builder atYLocation(MinMaxBounds.Doubles doubles) {
			return location().y(doubles);
		}

		public Builder x(MinMaxBounds.Doubles doubles) {
			this.x = doubles;
			return this;
		}

		public Builder y(MinMaxBounds.Doubles doubles) {
			this.y = doubles;
			return this;
		}

		public Builder z(MinMaxBounds.Doubles doubles) {
			this.z = doubles;
			return this;
		}

		public Builder biomes(HolderSet<Biome> holderSet) {
			this.biomes = Optional.of(holderSet);
			return this;
		}

		public Builder dimension(ResourceKey<Level> resourceKey) {
			this.dimension = Optional.of(resourceKey);
			return this;
		}

		public Builder light(LightSourceLightPredicate predicate) {
			this.light = Optional.of(predicate);
			return this;
		}

		public Builder smokey(boolean smokey) {
			this.smokey = Optional.of(smokey);
			return this;
		}

		public Builder canSeeSky(boolean canSeeSky) {
			this.canSeeSky = Optional.of(canSeeSky);
			return this;
		}

		public LightSourceLocationPredicate build() {
			Optional<PositionPredicate> position = PositionPredicate.of(this.x, this.y, this.z);
			return new LightSourceLocationPredicate(position, this.biomes, this.dimension, this.smokey, this.light, this.canSeeSky);
		}
	}

	public record PositionPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z) {
		public static final Codec<PositionPredicate> CODEC = RecordCodecBuilder.create(
				instance -> instance.group(
								MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::x),
								MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::y),
								MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::z)
						)
						.apply(instance, PositionPredicate::new)
		);

		public static Optional<PositionPredicate> of(
				MinMaxBounds.Doubles x,
				MinMaxBounds.Doubles y,
				MinMaxBounds.Doubles z
		) {
			return x.isAny() && y.isAny() && z.isAny()
					? Optional.empty()
					: Optional.of(new PositionPredicate(x, y, z));
		}

		public boolean matches(double x, double y, double z) {
			return this.x.matches(x) && this.y.matches(y) && this.z.matches(z);
		}
	}
}
