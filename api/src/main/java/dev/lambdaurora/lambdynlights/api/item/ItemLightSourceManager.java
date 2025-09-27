/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.item;

import dev.yumi.commons.event.Event;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Represents the item light source manager,
 * which provides the ability to register light sources for items, and to query their luminance.
 *
 * @author LambdAurora
 * @version 4.6.0
 * @see ItemLightSource
 * @since 3.0.0
 */
public interface ItemLightSourceManager {
	/**
	 * Represents the resource reloader identifier of item light sources.
	 *
	 * @since 4.6.0
	 */
	Identifier RESOURCE_RELOADER_ID = Identifier.of("lambdynlights", "item");

	/**
	 * {@return the registration event for item light sources}
	 */
	@NotNull Event<Identifier, OnRegister> onRegisterEvent();

	/**
	 * {@return the luminance value of the item stack}
	 *
	 * @param stack the item stack
	 */
	default @Range(from = 0, to = 15) int getLuminance(@NotNull ItemStack stack) {
		return this.getLuminance(stack, false);
	}

	/**
	 * {@return the luminance value of the item stack}
	 *
	 * @param stack the item stack
	 * @param submergedInWater {@code true} if the stack is submerged in water, else {@code false}
	 */
	@Range(from = 0, to = 15)
	int getLuminance(@NotNull ItemStack stack, boolean submergedInWater);

	/**
	 * Represents the registration event of item light sources.
	 */
	@FunctionalInterface
	interface OnRegister {
		/**
		 * Called when item light sources are registered.
		 *
		 * @param context the registration context
		 */
		void onRegister(@NotNull RegisterContext context);
	}

	/**
	 * Represents the registration context of item light sources.
	 */
	interface RegisterContext {
		/**
		 * {@return the lookup to registries}
		 *
		 * @since 4.6.0
		 */
		@NotNull HolderLookup.Provider registryLookup();

		/**
		 * {@return the access to registries}
		 *
		 * @deprecated Use {@link #registryLookup()} instead.
		 */
		@Deprecated(forRemoval = true, since = "4.6.0")
		default @NotNull RegistryAccess registryAccess() {
			return (RegistryAccess) this.registryLookup();
		}

		/**
		 * Registers the given item light source.
		 *
		 * @param itemLightSource the item light source to register
		 */
		void register(@NotNull ItemLightSource itemLightSource);

		/**
		 * Registers a light source of the given item with the given luminance.
		 *
		 * @param item the item to light up
		 * @param luminance the luminance of the item
		 * @see #register(ItemLightSource)
		 * @see #register(ItemLike, ItemLuminance)
		 */
		default void register(@NotNull ItemLike item, @Range(from = 0, to = 15) int luminance) {
			this.register(new ItemLightSource(
					ItemPredicate.Builder.item()
							.of(this.registryLookup().lookupOrThrow(Registries.ITEM), item)
							.build(),
					luminance
			));
		}

		/**
		 * Registers a light source of the given item with the given luminance.
		 *
		 * @param item the item to light up
		 * @param luminance the luminance of the item
		 * @see #register(ItemLightSource)
		 * @see #register(ItemLike, int)
		 */
		default void register(@NotNull ItemLike item, @NotNull ItemLuminance luminance) {
			this.register(new ItemLightSource(
					ItemPredicate.Builder.item()
							.of(this.registryLookup().lookupOrThrow(Registries.ITEM), item)
							.build(),
					luminance
			));
		}
	}
}
