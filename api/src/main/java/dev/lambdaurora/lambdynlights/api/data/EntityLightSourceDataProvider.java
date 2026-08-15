/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.data;

import dev.lambdaurora.lambdynlights.api.entity.EntityLightSource;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Provides the means to generate entity light source JSON files via data-generation.
 *
 * @author LambdAurora, Thomas Glasser
 * @version 4.1.0
 * @since 4.1.0
 */
public abstract class EntityLightSourceDataProvider
		extends LightSourceDataProvider<EntityLightSource, EntityLightSourceDataProvider.Context> {
	public EntityLightSourceDataProvider(
			PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryProvider, String defaultNamespace
	) {
		super(packOutput, registryProvider, defaultNamespace, "entity", EntityLightSource.CODEC);
	}

	@Override
	protected Context createContext(HolderLookup.Provider lookupProvider) {
		return new Context(lookupProvider);
	}

	/**
	 * Represents the data-generation context.
	 */
	public class Context extends LightSourceDataProvider<EntityLightSource, Context>.Context {
		protected Context(HolderLookup.Provider lookupProvider) {
			super(lookupProvider);
		}

		/**
		 * Adds a new entity light source to the provider.
		 *
		 * @param key the name of the JSON file
		 * @param predicate the predicate to select which entities emit the given luminance
		 * @param luminances the luminance sources
		 * @see #add(String, EntityLightSource.EntityPredicate, EntityLuminance...)
		 */
		public void add(
				Identifier key,
				EntityLightSource.EntityPredicate predicate, EntityLuminance... luminances
		) {
			this.add(key, new EntityLightSource(predicate, List.of(luminances)));
		}

		/**
		 * Adds a new entity light source to the provider.
		 *
		 * @param key the name of the JSON file within the default namespace
		 * @param predicate the predicate to select which entities emit the given luminance
		 * @param luminances the luminance sources
		 * @see #add(Identifier, EntityLightSource.EntityPredicate, EntityLuminance...)
		 */
		public void add(
				String key,
				EntityLightSource.EntityPredicate predicate, EntityLuminance... luminances
		) {
			this.add(this.idOf(key), predicate, luminances);
		}

		/**
		 * Adds a new entity light source to the provider.
		 *
		 * @param key the name of the JSON file
		 * @param type the entity type which emits the given luminance
		 * @param luminances the luminances to use
		 * @see #add(String, List, EntityLuminance...)
		 * @see #add(String, EntityType, EntityLuminance...)
		 * @see #add(String, List, EntityLuminance...)
		 * @see #add(EntityType, EntityLuminance...)
		 */
		public void add(Identifier key, EntityType<?> type, EntityLuminance... luminances) {
			this.add(key, EntityLightSource.EntityPredicate.builder().of(this.entityTypeLookup(), type).build(), luminances);
		}

		/**
		 * Adds a new entity light source to the provider.
		 *
		 * @param key the name of the JSON file
		 * @param types the entity types which emit the given luminance
		 * @param luminances the luminances to use
		 * @see #add(Identifier, EntityType, EntityLuminance...)
		 * @see #add(String, EntityType, EntityLuminance...)
		 * @see #add(String, List, EntityLuminance...)
		 * @see #add(EntityType, EntityLuminance...)
		 */
		@SuppressWarnings("deprecation")
		public void add(Identifier key, List<EntityType<?>> types, EntityLuminance... luminances) {
			var holderSet = HolderSet.direct(EntityType::builtInRegistryHolder, types);

			this.add(
					key,
					EntityLightSource.EntityPredicate.builder().entityType(new EntityTypePredicate(holderSet)).build(),
					luminances
			);
		}

		/**
		 * Adds a new entity light source to the provider.
		 *
		 * @param key the name of the JSON file within the default namespace
		 * @param type the entity type which emits the given luminance
		 * @param luminances the luminances to use
		 * @see #add(Identifier, EntityType, EntityLuminance...)
		 * @see #add(Identifier, List, EntityLuminance...)
		 * @see #add(String, List, EntityLuminance...)
		 * @see #add(EntityType, EntityLuminance...)
		 */
		public void add(String key, EntityType<?> type, EntityLuminance... luminances) {
			this.add(this.idOf(key), type, luminances);
		}

		/**
		 * Adds a new entity light source to the provider.
		 *
		 * @param key the name of the JSON file within the default namespace
		 * @param types the entity types which emit the given luminance
		 * @param luminances the luminances to use
		 * @see #add(Identifier, EntityType, EntityLuminance...)
		 * @see #add(Identifier, List, EntityLuminance...)
		 * @see #add(String, EntityType, EntityLuminance...)
		 * @see #add(EntityType, EntityLuminance...)
		 */
		public void add(
				String key, List<EntityType<?>> types, EntityLuminance... luminances
		) {
			this.add(this.idOf(key), types, luminances);
		}

		/**
		 * Adds a new entity light source to the provider.
		 * <p>
		 * The key is derived from the entity type's identifier following the rules of {@link #deriveId(Identifier)}.
		 *
		 * @param type the entity type which emits the given luminance
		 * @param luminances the luminances to use
		 * @see #add(Identifier, EntityType, EntityLuminance...)
		 * @see #add(Identifier, List, EntityLuminance...)
		 * @see #add(String, EntityType, EntityLuminance...)
		 * @see #add(String, List, EntityLuminance...)
		 */
		@SuppressWarnings("deprecation")
		public void add(EntityType<?> type, EntityLuminance... luminances) {
			var typeId = type.builtInRegistryHolder().key().identifier();
			this.add(this.deriveId(typeId), type, luminances);
		}

		/**
		 * Adds a new entity light source to the provider.
		 *
		 * @param key the name of the JSON file
		 * @param tag the tag that selects the entity types which emit the given luminance
		 * @param luminances the luminances to use
		 * @see #add(String, TagKey, EntityLuminance...)
		 * @see #add(TagKey, EntityLuminance...)
		 */
		public void add(
				Identifier key, TagKey<EntityType<?>> tag, EntityLuminance... luminances
		) {
			this.add(key, EntityLightSource.EntityPredicate.builder().of(this.entityTypeLookup(), tag).build(), luminances);
		}

		/**
		 * Adds a new entity light source to the provider.
		 *
		 * @param key the name of the JSON file
		 * @param tag the tag that selects the entity types which emit the given luminance
		 * @param luminances the luminances to use
		 * @see #add(Identifier, TagKey, EntityLuminance...)
		 * @see #add(TagKey, EntityLuminance...)
		 */
		public void add(
				String key, TagKey<EntityType<?>> tag, EntityLuminance... luminances
		) {
			this.add(this.idOf(key), tag, luminances);
		}

		/**
		 * Adds a new entity light source to this provider.
		 * <p>
		 * The key is derived from the tag's identifier following the rules of {@link #deriveId(Identifier)}.
		 *
		 * @param tag the tag that selects the entity types which emit the given luminance
		 * @param luminances the luminances to use
		 * @see #add(Identifier, TagKey, EntityLuminance...)
		 * @see #add(String, TagKey, EntityLuminance...)
		 */
		public void add(TagKey<EntityType<?>> tag, EntityLuminance... luminances) {
			this.add(this.deriveId(tag.location()), tag, luminances);
		}
	}
}
