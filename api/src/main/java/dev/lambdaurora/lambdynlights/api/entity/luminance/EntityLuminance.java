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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the provider of a luminance value of a given entity.
 *
 * @author LambdAurora
 * @version 4.1.0
 * @since 4.0.0
 */
public interface EntityLuminance {
	/**
	 * Represents the Codec of an entity luminance provider.
	 */
	Codec<EntityLuminance> CODEC = Codec.withAlternative(
			Type.CODEC.dispatch(EntityLuminance::type, Type::codec),
			Value.DIRECT_CODEC
	);
	/**
	 * Represents the Codec of a list of entity luminance providers.
	 */
	Codec<List<EntityLuminance>> LIST_CODEC = Codec.withAlternative(
			CODEC.listOf(),
			CODEC.xmap(List::of, List::getFirst)
	);

	/**
	 * {@return the type of this entity luminance}
	 */
	Type type();

	/**
	 * Gets the luminance of the given entity.
	 *
	 * @param itemLightSourceManager the item light source manager
	 * @param entity the entity to get the luminance of
	 * @return the luminance of the given entity
	 */
	@Range(from = 0, to = 15)
	int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity);

	/**
	 * Gets the luminance of the given entity out of the given list of entity luminance source.
	 *
	 * @param itemLightSourceManager the item light source manager
	 * @param entity the entity to get the luminance of
	 * @param luminances the entity luminance sources
	 * @return the luminance of the given entity
	 */
	static @Range(from = 0, to = 15) int getLuminance(
			ItemLightSourceManager itemLightSourceManager,
			Entity entity,
			List<EntityLuminance> luminances
	) {
		int luminance = 0;

		for (var luminanceSource : luminances) {
			int value = luminanceSource.getLuminance(itemLightSourceManager, entity);

			if (value > luminance) {
				luminance = value;
			}
		}

		return luminance;
	}

	/**
	 * Creates a new {@linkplain Value constant entity luminance} with the given value.
	 *
	 * @param luminance the luminance value between {@code 0} and {@code 15}
	 * @return the {@linkplain Value constant entity luminance} instance
	 * @since 4.1.0
	 */
	static Value of(@Range(from = 0, to = 15) int luminance) {
		return new Value(luminance);
	}

	/**
	 * Represents a direct entity luminance value.
	 *
	 * @param luminance the luminance
	 */
	record Value(@Range(from = 0, to = 15) int luminance) implements EntityLuminance {
		public static final Codec<Value> DIRECT_CODEC = Brightness.LIGHT_VALUE_CODEC.xmap(Value::new, Value::luminance);
		public static final MapCodec<Value> CODEC = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
						Brightness.LIGHT_VALUE_CODEC.fieldOf("value").forGetter(Value::luminance)
				).apply(instance, Value::new)
		);

		@Override
		public Type type() {
			return Type.VALUE;
		}

		@Override
		public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
			return this.luminance;
		}
	}

	/**
	 * Represents a type of entity luminance provider.
	 *
	 * @param id the identifier of this type
	 * @param codec the codec of this type
	 */
	record Type(Identifier id, MapCodec<? extends EntityLuminance> codec) {
		private static final Map<Identifier, Type> TYPES = new Object2ObjectOpenHashMap<>();
		/**
		 * Represents the Codec of an entity luminance provider type.
		 */
		public static final Codec<Type> CODEC = Identifier.CODEC.flatXmap(
				name -> Optional.ofNullable(TYPES.get(name))
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error(() -> "Unknown element name:" + name)),
				type -> DataResult.success(type.id)
		);

		public static final Type VALUE = register("value", Value.CODEC);
		public static final Type WATER_SENSITIVE = register("water_sensitive", WaterSensitiveEntityLuminance.CODEC);
		public static final Type WET_SENSITIVE = register("wet_sensitive", WetSensitiveEntityLuminance.CODEC);

		public static final Type ARROW_ITEM_DERIVED = registerSimple(
				"arrow/derived_from_self_item", ArrowItemDerivedLuminance.INSTANCE
		);
		public static final Type ENDERMAN = registerSimple("enderman", EndermanLuminance.INSTANCE);
		public static final Type GLOW_SQUID = registerSimple("glow_squid", GlowSquidLuminance.INSTANCE);
		public static final Type MAGMA_CUBE = registerSimple("magma_cube", MagmaCubeLuminance.INSTANCE);
		public static final Type FALLING_BLOCK = registerSimple("falling_block", FallingBlockLuminance.INSTANCE);
		public static final Type ITEM = register("item", ItemDerivedEntityLuminance.CODEC);
		public static final Type ITEM_ENTITY = registerSimple("item_entity", ItemEntityLuminance.INSTANCE);
		public static final Type ITEM_FRAME = registerSimple("item_frame", ItemFrameLuminance.INSTANCE);
		public static final Type MINECART_DISPLAY_BLOCK = registerSimple(
				"minecart/display_block", MinecartDisplayBlockLuminance.INSTANCE
		);
		public static final Type THROWABLE_ITEM = registerSimple(
				"projectile/throwable_item", ThrowableItemLuminance.INSTANCE
		);

		public static Type register(Identifier id, MapCodec<? extends EntityLuminance> codec) {
			var type = new Type(id, codec);
			TYPES.put(id, type);
			return type;
		}

		private static Type register(String name, MapCodec<? extends EntityLuminance> codec) {
			return register(Identifier.fromNamespaceAndPath("lambdynlights", name), codec);
		}

		public static Type registerSimple(Identifier id, EntityLuminance singleton) {
			return register(id, MapCodec.unit(singleton));
		}

		private static Type registerSimple(String name, EntityLuminance singleton) {
			return registerSimple(Identifier.fromNamespaceAndPath("lambdynlights", name), singleton);
		}
	}
}
