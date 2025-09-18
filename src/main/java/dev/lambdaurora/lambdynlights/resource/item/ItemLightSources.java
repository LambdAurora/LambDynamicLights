/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.resource.item;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSource;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.lambdaurora.lambdynlights.api.item.ItemLuminance;
import dev.lambdaurora.lambdynlights.api.predicate.ItemPredicate;
import dev.lambdaurora.lambdynlights.resource.LightSourceLoader;
import dev.lambdaurora.lambdynlights.resource.LoadedLightSourceResource;
import dev.yumi.commons.Either;
import dev.yumi.commons.event.Event;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents an item light sources manager.
 *
 * @author LambdAurora
 * @version 4.4.0
 * @since 1.3.0
 */
public final class ItemLightSources extends LightSourceLoader<ItemLightSource> implements ItemLightSourceManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambDynamicLights|ItemLightSources");
	public static final Identifier RESOURCE_RELOADER_ID = LambDynLights.id("item_dynamic_lights");

	private final Event<Identifier, OnRegister> onRegisterEvent = LambDynLights.EVENT_MANAGER.create(OnRegister.class);

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public Identifier getFabricId() {
		return RESOURCE_RELOADER_ID;
	}

	@Override
	protected String getResourcePath() {
		return "item";
	}

	@Override
	protected void doApply(RegistryAccess registryAccess, List<ItemLightSource> lightSources) {
		this.onRegisterEvent.invoker().onRegister(new RegisterContext() {
			@Override
			public @NotNull RegistryAccess registryAccess() {
				return registryAccess;
			}

			@Override
			public void register(@NotNull ItemLightSource itemLightSource) {
				lightSources.add(itemLightSource);
			}
		});
	}

	@Override
	protected @NotNull Optional<ItemLightSource> apply(DynamicOps<JsonElement> ops, LoadedLightSourceResource loadedData) {
		return this.attemptLoadLegacy(loadedData).fold(
				Function.identity(),
				legacyError -> {
					var loaded = ItemLightSource.CODEC.parse(ops, loadedData.data());

					if (!loadedData.silenceError() || LambDynLightsConstants.FORCE_LOG_ERRORS) {
						// Some files may choose to silence errors, especially if it's expected for some data to not always be present.
						// This should be used rarely to avoid issues.
						// Errors may be forced to be logged if the property "lambdynamiclights.resource.force_log_errors" is true
						// or if the environment is a development environment.
						loaded.error().ifPresent(error -> {
							LambDynLights.warn(LOGGER, "Failed to load item light source \"{}\" due to error: {}", loadedData.id(), error.message());
							LambDynLights.warn(LOGGER, "Also failed to load item light source \"{}\" in legacy mode due to error: {}", loadedData.id(), legacyError);
						});
					}
					return loaded.result();
				}
		);
	}

	/**
	 * Attempts to load legacy (pre v3) item light definitions.
	 *
	 * @param loadedData the item light definition to load
	 * @return an either which may have a left value if the item light loaded, or the error string as its right value
	 */
	private @NotNull Either<Optional<ItemLightSource>, String> attemptLoadLegacy(LoadedLightSourceResource loadedData) {
		if (!loadedData.data().has("item") || !loadedData.data().has("luminance")) {
			return Either.right("missing required fields (item, luminance).");
		}

		var affectId = new Identifier(loadedData.data().get("item").getAsString());
		var item = BuiltInRegistries.ITEM.get(affectId);

		if (item == Items.AIR)
			return Either.left(Optional.empty());

		boolean waterSensitive = false;
		if (loadedData.data().has("water_sensitive"))
			waterSensitive = loadedData.data().get("water_sensitive").getAsBoolean();

		var luminanceElement = loadedData.data().get("luminance").getAsJsonPrimitive();
		if (luminanceElement.isNumber()) {
			return Either.left(Optional.of(
					new ItemLightSource(
							ItemPredicate.Builder.item().of(item).build(),
							new ItemLuminance.Value(luminanceElement.getAsInt()),
							waterSensitive
					)
			));
		} else if (luminanceElement.isString()) {
			var luminanceStr = luminanceElement.getAsString();
			if (luminanceStr.equals("block")) {
				return Either.left(Optional.of(
						new ItemLightSource(
								ItemPredicate.Builder.item().of(item).build(),
								ItemLuminance.BlockSelf.INSTANCE,
								waterSensitive
						)
				));
			} else {
				var blockId = Identifier.tryParse(luminanceStr);
				if (blockId != null) {
					var block = BuiltInRegistries.BLOCK.get(blockId);
					if (block != Blocks.AIR)
						return Either.left(Optional.of(
								new ItemLightSource(
										ItemPredicate.Builder.item().of(item).build(),
										new ItemLuminance.BlockReference(block),
										waterSensitive
								)
						));
				}
			}
		} else {
			return Either.right("\"luminance\" field value isn't string or integer.");
		}

		return Either.left(Optional.empty());
	}

	@Override
	public @NotNull Event<Identifier, OnRegister> onRegisterEvent() {
		return this.onRegisterEvent;
	}

	@Override
	public int getLuminance(@NotNull ItemStack stack, boolean submergedInWater) {
		boolean shouldCareAboutWater = submergedInWater && LambDynLights.get().config.getWaterSensitiveCheck().get();

		int luminance = 0;
		boolean matchedAny = false;

		for (var data : this.lightSources) {
			if (data.predicate().test(stack)) {
				matchedAny = true;

				if (shouldCareAboutWater && data.waterSensitive()) continue;

				luminance = Math.max(luminance, data.getLuminance(stack));
			}
		}

		if (!matchedAny) {
			luminance = Block.byItem(stack.getItem()).defaultState().getLightEmission();
		}

		return luminance;
	}
}
