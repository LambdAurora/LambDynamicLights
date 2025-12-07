/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Provides the means to generate light source JSON files via data-generation.
 *
 * @param <L> the type of light source
 * @author LambdAurora, Thomas Glasser
 * @version 4.1.0
 * @since 4.1.0
 */
public abstract class LightSourceDataProvider<L, C extends LightSourceDataProvider<L, C>.Context>
		implements DataProvider {
	private final PackOutput.PathProvider pathProvider;
	private final CompletableFuture<HolderLookup.Provider> registryProvider;
	private final String defaultNamespace;
	private final String subPath;
	private final Codec<L> codec;

	public LightSourceDataProvider(
			PackOutput packOutput,
			CompletableFuture<HolderLookup.Provider> registryProvider,
			String defaultNamespace,
			String subPath,
			Codec<L> codec
	) {
		this.registryProvider = registryProvider;
		this.defaultNamespace = defaultNamespace;
		this.pathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "dynamiclights/" + subPath);
		this.subPath = subPath;
		this.codec = codec;
	}

	/**
	 * {@return the default namespace used for this light source data provider}
	 */
	public String defaultNamespace() {
		return this.defaultNamespace;
	}

	/**
	 * Creates the context for data-generation.
	 *
	 * @param lookupProvider the lookup provider
	 * @return the context
	 */
	protected abstract C createContext(HolderLookup.Provider lookupProvider);

	/**
	 * Generates the light sources and adds them to the list.
	 *
	 * @param context the light source data generation context
	 */
	protected abstract void generate(C context);

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		return this.registryProvider.thenCompose(provider -> {
			var context = this.createContext(provider);
			this.generate(context);
			return CompletableFuture.allOf(
					context.sources.entrySet().stream()
							.map(entry -> DataProvider.saveStable(
									cachedOutput, provider, this.codec, entry.getValue(), this.pathProvider.json(entry.getKey()))
							).toArray(CompletableFuture[]::new)
			);
		});
	}

	@Override
	public String getName() {
		return this.subPath + " Light Sources Provider";
	}

	/**
	 * Represents the data-generation context.
	 */
	protected abstract class Context {
		private final HolderLookup.Provider lookupProvider;
		private final HolderLookup<Item> itemLookup;
		private final HolderLookup<EntityType<?>> entityTypeLookup;

		protected final Map<Identifier, L> sources = new HashMap<>();

		protected Context(HolderLookup.Provider lookupProvider) {
			this.lookupProvider = lookupProvider;
			this.itemLookup = this.lookupProvider.lookupOrThrow(Registries.ITEM);
			this.entityTypeLookup = this.lookupProvider.lookupOrThrow(Registries.ENTITY_TYPE);
		}

		/**
		 * {@return the lookup provider}
		 */
		public HolderLookup.Provider lookupProvider() {
			return this.lookupProvider;
		}

		/**
		 * {@return the item lookup}
		 */
		public HolderLookup<Item> itemLookup() {
			return this.itemLookup;
		}

		/**
		 * {@return the entity type lookup}
		 */
		public HolderLookup<EntityType<?>> entityTypeLookup() {
			return this.entityTypeLookup;
		}

		/**
		 * Creates an identifier of the given {@code path} and the default namespace given to the light source data provider.
		 *
		 * @param path the path of the identifier
		 * @return the identifier of the given {@code path} and the default namespace
		 */
		public Identifier idOf(String path) {
			return Identifier.fromNamespaceAndPath(LightSourceDataProvider.this.defaultNamespace, path);
		}

		/**
		 * Creates an identifier that's derived from the given identifier as follows:
		 * <ul>
		 *     <li>the resulting identifier is within the default namespace;</li>
		 *     <li>
		 *         the path may be prefixed by a subdirectory of the given identifier's namespace
		 *         if the namespace is different from the default namespace and {@value  Identifier#DEFAULT_NAMESPACE};
		 *     </li>
		 *     <li>the rest of the path is the given identifier's path.</li>
		 * </ul>
		 *
		 * @param originalId the identifier to derive from
		 * @return the derived identifier
		 */
		public Identifier deriveId(Identifier originalId) {
			var id = originalId;

			if (!id.getNamespace().equals(LightSourceDataProvider.this.defaultNamespace())) {
				// The namespace is different:
				id = this.idOf(originalId.getPath());

				if (!originalId.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
					id = id.withPrefix(originalId.getNamespace() + "/");
				}
			}

			return id;
		}

		/**
		 * Adds a new light source to the provider.
		 *
		 * @param id the name of the JSON file
		 * @param source the light source
		 * @see #add(String, Object)
		 */
		public void add(Identifier id, L source) {
			this.sources.put(id, source);
		}

		/**
		 * Adds a new light source to the provider.
		 *
		 * @param key the name of the JSON file within the default namespace
		 * @param source The light source
		 * @see #add(Identifier, Object)
		 */
		public void add(String key, L source) {
			this.add(this.idOf(key), source);
		}

		/**
		 * {@return the sources to be registered}
		 */
		public Map<Identifier, L> sources() {
			return this.sources;
		}
	}
}
