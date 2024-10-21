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
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Represents the item light source manager,
 * which provides the ability to register light sources to items, and to query the luminance of item stacks.
 *
 * @author LambdAurora
 * @version 3.0.0
 * @since 3.0.0
 */
public interface ItemLightSourceManager {
	/**
	 * {@return the registration event for item light sources}
	 */
	Event<Identifier, OnRegister> onRegisterEvent();

	/**
	 * {@return the luminance value of the item stack}
	 *
	 * @param stack the item stack
	 */
	default int getLuminance(ItemStack stack) {
		return this.getLuminance(stack, false);
	}

	/**
	 * {@return the luminance value of the item stack}
	 *
	 * @param stack the item stack
	 * @param submergedInWater {@code true} if the stack is submerged in water, else {@code false}
	 */
	int getLuminance(ItemStack stack, boolean submergedInWater);

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
		void onRegister(RegisterContext context);
	}

	interface RegisterContext {
		/**
		 * {@return the access to registries}
		 */
		RegistryAccess registryAccess();

		/**
		 * Registers the given item light source.
		 *
		 * @param itemLightSource the item light source to register
		 */
		void register(ItemLightSource itemLightSource);

		/**
		 * Registers a light source of the given item with the given luminance.
		 *
		 * @param item the item to light
		 * @param luminance the luminance of the item
		 * @see #register(ItemLightSource)
		 * @see #register(ItemLike, ItemLuminance)
		 */
		default void register(ItemLike item, int luminance) {
			this.register(new ItemLightSource(
					ItemPredicate.Builder.item()
							.of(this.registryAccess().lookupOrThrow(Registries.ITEM), item)
							.build(),
					luminance
			));
		}

		/**
		 * Registers a light source of the given item with the given luminance.
		 *
		 * @param item the item to light
		 * @param luminance the luminance of the item
		 * @see #register(ItemLightSource)
		 * @see #register(ItemLike, int)
		 */
		default void register(ItemLike item, ItemLuminance luminance) {
			this.register(new ItemLightSource(
					ItemPredicate.Builder.item()
							.of(this.registryAccess().lookupOrThrow(Registries.ITEM), item)
							.build(),
					luminance
			));
		}
	}
}
