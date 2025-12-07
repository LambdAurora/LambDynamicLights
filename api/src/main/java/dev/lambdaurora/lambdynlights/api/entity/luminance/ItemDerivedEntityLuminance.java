/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.entity.luminance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides the luminance value of a given item.
 *
 * @param item the item to derive the luminance value from
 * @param includeRain {@code true} if the wetness check should include rain, or {@code false} otherwise
 * @param always let the item be always considered dry or wet if present
 * @author LambdAurora
 * @version 4.2.0
 * @since 4.0.0
 */
public record ItemDerivedEntityLuminance(
		ItemStack item,
		boolean includeRain,
		Optional<Always> always
) implements EntityLuminance {
	public static final MapCodec<ItemDerivedEntityLuminance> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					ItemStack.CODEC.fieldOf("item").forGetter(ItemDerivedEntityLuminance::item),
					Codec.BOOL.optionalFieldOf("include_rain", false).forGetter(ItemDerivedEntityLuminance::includeRain),
					Always.CODEC.optionalFieldOf("always").forGetter(ItemDerivedEntityLuminance::always)
			).apply(instance, ItemDerivedEntityLuminance::new)
	);

	@Override
	public Type type() {
		return Type.ITEM;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		boolean wet = this.always.map(value -> switch (value) {
			case DRY -> false;
			case WET -> true;
		}).orElseGet(() -> this.includeRain ? entity.isInWaterOrRain() : entity.isUnderWater());

		return itemLightSourceManager.getLuminance(this.item, wet);
	}

	/**
	 * Represents whether the item should always be considered dry or wet.
	 */
	public enum Always {
		DRY,
		WET;

		private static final Map<String, Always> BY_NAME = Util.make(
				() -> Stream.of(values()).collect(HashMap::new, (map, type) -> map.put(type.getName(), type), HashMap::putAll)
		);
		public static final Codec<Always> CODEC = Codec.stringResolver(Always::getName, Always::byName);

		private final String name = this.name().toLowerCase();

		/**
		 * {@return the name of this {@code Always} value}
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * {@return the {@code Always} value from its name}
		 *
		 * @param name the name of the {@code Always} value
		 */
		public static Always byName(String name) {
			return BY_NAME.get(name);
		}
	}

	/**
	 * Creates a new {@link ItemDerivedEntityLuminance} builder.
	 *
	 * @param item the item to derive the luminance value from
	 * @return the builder
	 * @since 4.1.0
	 */
	public static Builder builder(ItemStack item) {
		return new Builder(item);
	}

	/**
	 * Represents a builder for creating new {@link ItemDerivedEntityLuminance} instances.
	 *
	 * @since 4.1.0
	 */
	public static class Builder {
		private final ItemStack item;
		private boolean includeRain = false;
		private Optional<Always> always = Optional.empty();

		private Builder(ItemStack item) {
			this.item = item;
		}

		/**
		 * Sets whether the wetness check should include rain.
		 *
		 * @param includeRain {@code true} if the wetness check should include rain, or {@code false} otherwise
		 * @return {@code this}
		 */
		@Contract("_ -> this")
		public Builder includeRain(boolean includeRain) {
			this.includeRain = includeRain;
			return this;
		}

		/**
		 * Sets in which state the item should always be considered.
		 *
		 * @param always the state of the item it should always be considered in
		 * @return {@code this}
		 */
		@Contract("_ -> this")
		public Builder always(@Nullable Always always) {
			this.always = Optional.ofNullable(always);
			return this;
		}

		/**
		 * Builds the resulting {@link ItemDerivedEntityLuminance}.
		 *
		 * @return the resulting {@link ItemDerivedEntityLuminance}
		 */
		public ItemDerivedEntityLuminance build() {
			return new ItemDerivedEntityLuminance(this.item, this.includeRain, this.always);
		}
	}
}
