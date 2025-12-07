/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.data;

import dev.lambdaurora.lambdynlights.api.item.ItemLightSource;
import dev.lambdaurora.lambdynlights.api.item.ItemLuminance;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

/**
 * Provides the means to generate item light source JSON files via data-generation.
 *
 * @author LambdAurora, Thomas Glasser
 * @version 4.1.0
 * @since 4.1.0
 */
public abstract class ItemLightSourceDataProvider
		extends LightSourceDataProvider<ItemLightSource, ItemLightSourceDataProvider.Context> {
	public ItemLightSourceDataProvider(
			PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryProvider, String defaultNamespace
	) {
		super(packOutput, registryProvider, defaultNamespace, "item", ItemLightSource.CODEC);
	}

	@Override
	protected ItemLightSourceDataProvider.Context createContext(HolderLookup.Provider lookupProvider) {
		return new Context(lookupProvider);
	}

	/**
	 * Represents the data-generation context.
	 */
	protected class Context extends LightSourceDataProvider<ItemLightSource, Context>.Context {
		protected Context(HolderLookup.Provider lookupProvider) {
			super(lookupProvider);
		}

		/**
		 * Adds a new item light source to this provider.
		 *
		 * @param key the identifier of the JSON file
		 * @param predicate the predicate to select which items emit the given luminance
		 * @param luminance the luminance to emit
		 * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
		 * @see #add(String, ItemPredicate, ItemLuminance, boolean)
		 */
		public void add(
				Identifier key,
				ItemPredicate predicate, ItemLuminance luminance, boolean waterSensitive
		) {
			this.add(key, new ItemLightSource(predicate, luminance, waterSensitive));
		}

		/**
		 * Adds a new item light source to this provider.
		 *
		 * @param key the name of the JSON file within the default namespace
		 * @param predicate the predicate to select which items emit the given luminance
		 * @param luminance the luminance to emit
		 * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
		 * @see #add(Identifier, ItemPredicate, ItemLuminance, boolean)
		 */
		public void add(
				String key,
				ItemPredicate predicate, ItemLuminance luminance, boolean waterSensitive
		) {
			this.add(this.idOf(key), predicate, luminance, waterSensitive);
		}

		/**
		 * Adds a new item light source to this provider.
		 *
		 * @param key the name of the JSON file
		 * @param luminance the luminance to emit
		 * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
		 * @param items the items which emit the given luminance
		 * @see #add(String, ItemLuminance, boolean, ItemLike...)
		 * @see #add(ItemLike, ItemLuminance, boolean)
		 */
		public void add(
				Identifier key,
				ItemLuminance luminance, boolean waterSensitive, ItemLike... items
		) {
			this.add(key, ItemPredicate.Builder.item().of(this.itemLookup(), items).build(), luminance, waterSensitive);
		}

		/**
		 * Adds a new item light source to this provider.
		 *
		 * @param key the name of the JSON file within the default namespace
		 * @param luminance the luminance to emit
		 * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
		 * @param items the items which emit the given luminance
		 * @see #add(Identifier, ItemLuminance, boolean, ItemLike...)
		 * @see #add(ItemLike, ItemLuminance, boolean)
		 */
		public void add(
				String key,
				ItemLuminance luminance, boolean waterSensitive, ItemLike... items
		) {
			this.add(this.idOf(key), luminance, waterSensitive, items);
		}

		/**
		 * Adds a new item light source to this provider.
		 * <p>
		 * The key is derived from the item's identifier following the rules of {@link #deriveId(Identifier)}.
		 *
		 * @param item the item which emits the given luminance
		 * @param luminance the luminance to emit
		 * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
		 * @see #add(Identifier, ItemLuminance, boolean, ItemLike...)
		 * @see #add(String, ItemLuminance, boolean, ItemLike...)
		 */
		@SuppressWarnings("deprecation")
		public void add(ItemLike item, ItemLuminance luminance, boolean waterSensitive) {
			var itemId = item.asItem().builtInRegistryHolder().key().identifier();
			this.add(this.deriveId(itemId), luminance, waterSensitive, item);
		}

		/**
		 * Adds a new item light source to this provider.
		 *
		 * @param key the name of the JSON file within the default namespace
		 * @param tag the tag that selects the items which emit the given luminance
		 * @param luminance the luminance to emit
		 * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
		 * @see #add(String, TagKey, ItemLuminance, boolean)
		 * @see #add(TagKey, ItemLuminance, boolean)
		 */
		public void add(
				Identifier key,
				TagKey<Item> tag, ItemLuminance luminance, boolean waterSensitive
		) {
			this.add(key, ItemPredicate.Builder.item().of(this.itemLookup(), tag).build(), luminance, waterSensitive);
		}

		/**
		 * Adds a new item light source to this provider.
		 *
		 * @param key the name of the JSON file within the default namespace
		 * @param tag the tag that selects the items which emit the given luminance
		 * @param luminance the luminance to emit
		 * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
		 * @see #add(Identifier, TagKey, ItemLuminance, boolean)
		 * @see #add(TagKey, ItemLuminance, boolean)
		 */
		public void add(
				String key,
				TagKey<Item> tag, ItemLuminance luminance, boolean waterSensitive
		) {
			this.add(this.idOf(key), tag, luminance, waterSensitive);
		}

		/**
		 * Adds a new item light source to this provider.
		 * <p>
		 * The key is derived from the tag's identifier following the rules of {@link #deriveId(Identifier)}.
		 *
		 * @param tag the tag that selects the items which emit the given luminance
		 * @param luminance the luminance to emit
		 * @param waterSensitive {@code true} if this light source is sensitive to water, or {@code false} otherwise
		 * @see #add(Identifier, TagKey, ItemLuminance, boolean)
		 * @see #add(String, TagKey, ItemLuminance, boolean)
		 */
		public void add(TagKey<Item> tag, ItemLuminance luminance, boolean waterSensitive) {
			this.add(this.deriveId(tag.location()), tag, luminance, waterSensitive);
		}
	}
}
