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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Brightness;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents the luminance value of an item.
 *
 * @author LambdAurora
 * @version 3.0.0
 * @since 3.0.0
 */
public sealed interface ItemLuminance permits ItemLuminance.Value, ItemLuminance.BlockReference, ItemLuminance.BlockSelf {
	Codec<ItemLuminance> CODEC = Codec.withAlternative(
			Type.CODEC.dispatch(ItemLuminance::type, Type::codec),
			Value.DIRECT_CODEC
	);

	/**
	 * {@return the type of this item luminance}
	 */
	Type type();

	/**
	 * Gets the luminance of the given item stack.
	 *
	 * @param stack the item stack to get the luminance of
	 * @return the luminance of the given item stack
	 */
	@Range(from = 0, to = 15)
	int getLuminance(ItemStack stack);

	/**
	 * Represents a direct item luminance value.
	 *
	 * @param luminance the luminance
	 */
	record Value(@Range(from = 0, to = 15) int luminance) implements ItemLuminance {
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
		public @Range(from = 0, to = 15) int getLuminance(ItemStack stack) {
			return this.luminance;
		}
	}

	/**
	 * Represents an item luminance value that's derived from the referenced block's default state.
	 *
	 * @param block the referenced block
	 */
	record BlockReference(Block block) implements ItemLuminance {
		public static final MapCodec<BlockReference> CODEC = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
						BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(BlockReference::blockHolder)
				).apply(instance, BlockReference::new)
		);

		public BlockReference(Holder<Block> block) {
			this(block.value());
		}

		@SuppressWarnings("deprecation")
		public Holder<Block> blockHolder() {
			return this.block.builtInRegistryHolder();
		}

		@Override
		public Type type() {
			return Type.BLOCK_REFERENCE;
		}

		@Override
		public @Range(from = 0, to = 15) int getLuminance(ItemStack stack) {
			return this.block.defaultState().getLightEmission();
		}
	}

	/**
	 * Represents the item luminance value that's derived from the block the item represents.
	 */
	final class BlockSelf implements ItemLuminance {
		public static final BlockSelf INSTANCE = new BlockSelf();
		public static final MapCodec<BlockSelf> CODEC = MapCodec.unit(INSTANCE);

		private BlockSelf() {}

		@Override
		public Type type() {
			return Type.BLOCK_SELF;
		}

		@Override
		public @Range(from = 0, to = 15) int getLuminance(ItemStack stack) {
			return Block.byItem(stack.getItem()).defaultState().getLightEmission();
		}
	}

	/**
	 * Represents the type of item luminance value.
	 */
	enum Type {
		VALUE("value", Value.CODEC),
		BLOCK_REFERENCE("block", BlockReference.CODEC),
		BLOCK_SELF("block_self", BlockSelf.CODEC);

		private static final Map<String, Type> BY_NAME = Util.make(
				() -> Stream.of(values()).collect(HashMap::new, (map, type) -> map.put(type.getName(), type), HashMap::putAll)
		);
		public static final Codec<Type> CODEC = Codec.stringResolver(Type::getName, Type::byName);

		private final String name;
		private final MapCodec<? extends ItemLuminance> codec;

		Type(String name, MapCodec<? extends ItemLuminance> codec) {
			this.name = name;
			this.codec = codec;
		}

		public String getName() {
			return this.name;
		}

		public MapCodec<? extends ItemLuminance> codec() {
			return this.codec;
		}

		public static Type byName(String name) {
			return BY_NAME.get(name);
		}
	}
}
