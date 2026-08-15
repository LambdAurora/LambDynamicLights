/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.accessor.DynamicLightHandlerHolder;
import dev.lambdaurora.lambdynlights.config.LightSourceSettingEntry;
import dev.lambdaurora.spruceui.tooltip.TooltipData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin<T extends Entity> implements DynamicLightHandlerHolder<T> {
	@Shadow
	public abstract Component getDescription();

	@Shadow
	public abstract String getDescriptionId();

	@Unique
	private LightSourceSettingEntry lambdynlights$setting;

	@Override
	public @Nullable LightSourceSettingEntry lambdynlights$getSetting() {
		if (this.lambdynlights$setting == null) {
			var self = (EntityType<?>) (Object) this;
			var id = BuiltInRegistries.ENTITY_TYPE.getKey(self);
			if (id.getNamespace().equals("minecraft") && id.getPath().equals("pig") && self != EntityType.PIG) {
				return null;
			}

			String key = id.getNamespace() + "." + id.getPath();

			this.lambdynlights$setting = new LightSourceSettingEntry(key, this.getDescriptionId(), true, null, TooltipData.EMPTY);
			LambDynLights.get().config.load(this.lambdynlights$setting);
		}

		return this.lambdynlights$setting;
	}

	@Override
	public Component lambdynlights$getName() {
		var name = this.getDescription();
		if (name == null) {
			return Component.translatable("lambdynlights.dummy");
		}
		return name;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Identifier lambdynlights$getId() {
		return EntityType.getKey((EntityType) (Object) this);
	}
}
