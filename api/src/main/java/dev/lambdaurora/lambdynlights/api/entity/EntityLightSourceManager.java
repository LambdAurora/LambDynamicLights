/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.entity;

import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.yumi.commons.event.Event;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.Optional;

/**
 * Represents the entity light source manager,
 * which provides the ability to register light sources for entities, and to query their luminance.
 *
 * @author LambdAurora
 * @version 4.6.0
 * @see EntityLightSource
 * @since 4.0.0
 */
public interface EntityLightSourceManager {
	/**
	 * Represents the resource reloader identifier of item light sources.
	 *
	 * @since 4.6.0
	 */
	Identifier RESOURCE_RELOADER_ID = Identifier.fromNamespaceAndPath("lambdynlights", "entity");

	/**
	 * {@return the registration event for entity light sources}
	 */
	Event<Identifier, OnRegister> onRegisterEvent();

	/**
	 * {@return the luminance value of the entity}
	 *
	 * @param entity the entity
	 */
	@Range(from = 0, to = 15)
	int getLuminance(Entity entity);

	/**
	 * Represents the registration event of entity light sources.
	 */
	@FunctionalInterface
	interface OnRegister {
		/**
		 * Called when entity light sources are registered.
		 *
		 * @param context the registration context
		 */
		void onRegister(RegisterContext context);
	}

	/**
	 * Represents the registration context of entity light sources.
	 */
	interface RegisterContext {
		/**
		 * {@return the lookup to registries}
		 *
		 * @since 4.6.0
		 */
		HolderLookup.Provider registryLookup();

		/**
		 * {@return the access to registries}
		 *
		 * @deprecated Use {@link #registryLookup()} instead.
		 */
		@Deprecated(forRemoval = true, since = "4.6.0")
		default RegistryAccess registryAccess() {
			return (RegistryAccess) this.registryLookup();
		}

		/**
		 * Registers the given entity light source.
		 *
		 * @param entityLightSource the entity light source to register
		 */
		void register(EntityLightSource entityLightSource);

		/**
		 * Registers a light source of the given entity with the given luminance.
		 *
		 * @param entityType the type of entity to light up
		 * @param luminance the luminance of the entity
		 * @see #register(EntityLightSource)
		 * @see #register(EntityType, EntityLuminance...)
		 */
		default void register(EntityType<?> entityType, @Range(from = 0, to = 15) int luminance) {
			this.register(new EntityLightSource(
					new EntityLightSource.EntityPredicate(
							Optional.of(EntityTypePredicate.of(
									this.registryLookup().lookupOrThrow(Registries.ENTITY_TYPE), entityType
							)),
							Optional.empty(),
							Optional.empty(),
							Optional.empty(),
							Optional.empty(),
							Optional.empty(),
							Optional.empty(),
							Optional.empty(),
							Optional.empty()
					),
					List.of(new EntityLuminance.Value(luminance))
			));
		}

		/**
		 * Registers a light source of the given entity with the given luminance.
		 *
		 * @param entityType the type of entity to light up
		 * @param luminance the luminance of the entity
		 * @see #register(EntityLightSource)
		 * @see #register(EntityType, int)
		 */
		default void register(EntityType<?> entityType, EntityLuminance... luminance) {
			this.register(new EntityLightSource(
					new EntityLightSource.EntityPredicate(
							Optional.of(EntityTypePredicate.of(
									this.registryLookup().lookupOrThrow(Registries.ENTITY_TYPE), entityType
							)),
							Optional.empty(),
							Optional.empty(),
							Optional.empty(),
							Optional.empty(),
							Optional.empty(),
							Optional.empty(),
							Optional.empty(),
							Optional.empty()
					),
					List.of(luminance)
			));
		}
	}
}
