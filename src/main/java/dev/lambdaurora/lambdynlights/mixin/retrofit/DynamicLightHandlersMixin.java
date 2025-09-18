/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.retrofit;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandler;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.entity.luminance.WaterSensitiveEntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@SuppressWarnings("removal")
@Mixin(value = DynamicLightHandlers.class, remap = false)
public final class DynamicLightHandlersMixin {
	@Unique
	private static final EntityLuminance.Type DUMMY_RETROFIT_TYPE = new EntityLuminance.Type(
			LambDynLights.id("dummy/old_dynamic_light_handlers"), null
	);

	@Accessor(remap = false)
	private static Map<EntityType<?>, DynamicLightHandler<?>> getHANDLERS() {
		throw new IllegalStateException("Injection failed.");
	}

	/**
	 * Registers the default handlers.
	 */
	@SuppressWarnings("unchecked")
	@Inject(method = "registerDefaultHandlers", at = @At("TAIL"), remap = false)
	private static void onRegisterDefaultHandlers(CallbackInfo ci) {
		LambDynLights.get().entityLightSourceManager().onRegisterEvent().register(context -> {
			getHANDLERS().forEach((type, handler) -> {
				var actualHandler = (DynamicLightHandler<Entity>) handler;

				context.register(type, new WaterSensitiveEntityLuminance(
						List.of(lambdynlights$retrofitHandler(actualHandler, false)),
						List.of(lambdynlights$retrofitHandler(actualHandler, true))
				));
			});
		});
	}

	/**
	 * @author LambdAurora
	 * @reason Implementation of the deprecated DynamicLightHandlers#canLightUp API.
	 */
	@Overwrite(remap = false)
	public static <T extends Entity> boolean canLightUp(T entity) {
		return DynamicLightingEngine.canLightUp(entity);
	}

	/**
	 * @author LambdAurora
	 * @reason Implementation of the deprecated DynamicLightHandlers#getLuminanceFrom API.
	 */
	@Overwrite(remap = false)
	public static <T extends Entity> int getLuminanceFrom(T entity) {
		return LambDynLights.getLuminanceFrom(entity);
	}

	@Unique
	private static EntityLuminance lambdynlights$retrofitHandler(DynamicLightHandler<Entity> handler, boolean inWater) {
		return new EntityLuminance() {
			@Override
			public @NotNull Type type() {
				return DUMMY_RETROFIT_TYPE;
			}

			@Override
			public @Range(from = 0, to = 15) int getLuminance(
					@NotNull ItemLightSourceManager itemLightSourceManager,
					@NotNull Entity entity
			) {
				if (handler.isWaterSensitive(entity) && inWater) {
					return 0;
				} else {
					return handler.getLuminance(entity);
				}
			}
		};
	}
}
