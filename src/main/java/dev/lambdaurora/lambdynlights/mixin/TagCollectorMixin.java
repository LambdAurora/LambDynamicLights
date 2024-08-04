/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.resource.item.ItemLightSources;
import net.minecraft.client.multiplayer.TagCollector;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(TagCollector.class)
public class TagCollectorMixin {
	@Inject(
			method = "applyTags(Lnet/minecraft/core/RegistryAccess;Ljava/util/function/Predicate;)V",
			at = @At("RETURN")
	)
	private void onTagSynchronization(
			RegistryAccess registryAccess, Predicate<ResourceKey<? extends Registry<?>>> predicate,
			CallbackInfo ci
	) {
		LambDynLights.get().itemLightSources.apply(registryAccess);
	}
}
