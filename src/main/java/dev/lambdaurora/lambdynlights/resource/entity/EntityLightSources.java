/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.resource.entity;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import dev.lambdaurora.lambdynlights.api.entity.EntityLightSource;
import dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.lambdaurora.lambdynlights.resource.LightSourceLoader;
import dev.lambdaurora.lambdynlights.resource.LoadedLightSourceResource;
import dev.lambdaurora.lambdynlights.resource.entity.luminance.CreeperLuminance;
import dev.lambdaurora.lambdynlights.resource.entity.luminance.DisplayEntityLuminance;
import dev.lambdaurora.lambdynlights.resource.item.ItemLightSources;
import dev.yumi.commons.event.Event;
import dev.yumi.mc.core.api.YumiEvents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Represents an entity light source manager.
 *
 * @author LambdAurora
 * @version 4.5.0
 * @since 4.0.0
 */
public final class EntityLightSources extends LightSourceLoader<EntityLightSource> implements EntityLightSourceManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambDynamicLights|EntityLightSources");
	private static final Identifier RESOURCE_RELOADER_ID = LambDynLights.id("entity_dynamic_lights");
	private static final List<Identifier> RESOURCE_RELOADER_DEPENDENCIES = List.of(ItemLightSources.RESOURCE_RELOADER_ID);

	public static final EntityLuminance.Type CREEPER = EntityLuminance.Type.registerSimple(
			LambDynLights.id("creeper"), CreeperLuminance.INSTANCE
	);

	public static final EntityLuminance.Type DISPLAY = EntityLuminance.Type.register(
			LambDynLights.id("display"), DisplayEntityLuminance.CODEC
	);
	public static final EntityLuminance.Type BLOCK_DISPLAY = EntityLuminance.Type.registerSimple(
			LambDynLights.id("display/block"), DisplayEntityLuminance.BlockDisplayLuminance.INSTANCE
	);
	public static final EntityLuminance.Type ITEM_DISPLAY = EntityLuminance.Type.registerSimple(
			LambDynLights.id("display/item"), DisplayEntityLuminance.ItemDisplayLuminance.INSTANCE
	);

	private final Event<Identifier, OnRegister> onRegisterEvent = YumiEvents.EVENTS.create(OnRegister.class);
	private final ItemLightSourceManager itemLightSourceManager;

	public EntityLightSources(
			ItemLightSourceManager itemLightSourceManager,
			ApplicationPredicate applicationPredicate
	) {
		super(applicationPredicate);
		this.itemLightSourceManager = itemLightSourceManager;
	}

	@Override
	public @NotNull Identifier id() {
		return RESOURCE_RELOADER_ID;
	}

	@Override
	public @Unmodifiable Collection<Identifier> dependencies() {
		return RESOURCE_RELOADER_DEPENDENCIES;
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String getResourcePath() {
		return "entity";
	}

	@Override
	protected void doApply(RegistryAccess registryAccess, List<EntityLightSource> lightSources) {
		this.onRegisterEvent.invoker().onRegister(new RegisterContext() {
			@Override
			public @NotNull RegistryAccess registryAccess() {
				return registryAccess;
			}

			@Override
			public void register(@NotNull EntityLightSource entityLightSource) {
				lightSources.add(entityLightSource);
			}
		});
	}

	@Override
	protected @NotNull Optional<EntityLightSource> apply(DynamicOps<JsonElement> ops, LoadedLightSourceResource loadedData) {
		var loaded = EntityLightSource.CODEC.parse(ops, loadedData.data());

		if (!loadedData.silenceError() || LambDynLightsConstants.FORCE_LOG_ERRORS) {
			// Some files may choose to silence errors, especially if it's expected for some data to not always be present.
			// This should be used rarely to avoid issues.
			// Errors may be forced to be logged if the property "lambdynamiclights.resource.force_log_errors" is true
			// or if the environment is a development environment.
			loaded.ifError(error -> {
				LambDynLights.warn(LOGGER, "Failed to load entity light source \"{}\" due to error: {}",
						loadedData.id(), error.message()
				);
			});
		}

		return loaded.result();
	}

	@Override
	public @NotNull Event<Identifier, OnRegister> onRegisterEvent() {
		return this.onRegisterEvent;
	}

	@Override
	public int getLuminance(@NotNull Entity entity) {
		int luminance = 0;

		for (var data : this.lightSources) {
			if (luminance == 15) {
				// We already achieved the maximum light value, no need to execute the other luminance providers.
				break;
			} else if (data.predicate().test(entity)) {
				luminance = Math.max(luminance, data.getLuminance(this.itemLightSourceManager, entity));
			}
		}

		return luminance;
	}
}
