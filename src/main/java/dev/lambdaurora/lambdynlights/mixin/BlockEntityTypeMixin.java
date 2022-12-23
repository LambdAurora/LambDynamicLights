/*
 * Copyright © 2020-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.accessor.DynamicLightHandlerHolder;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandler;
import dev.lambdaurora.lambdynlights.config.LightSourceSettingEntry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.Text;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin<T extends BlockEntity> implements DynamicLightHandlerHolder<T> {
	@Unique
	private DynamicLightHandler<T> lambdynlights$lightHandler;
	@Unique
	private LightSourceSettingEntry lambdynlights$setting;

	@Override
	public @Nullable DynamicLightHandler<T> lambdynlights$getDynamicLightHandler() {
		return this.lambdynlights$lightHandler;
	}

	@Override
	public void lambdynlights$setDynamicLightHandler(DynamicLightHandler<T> handler) {
		this.lambdynlights$lightHandler = handler;
	}

	@Override
	public LightSourceSettingEntry lambdynlights$getSetting() {
		if (this.lambdynlights$setting == null) {
			var self = (BlockEntityType<?>) (Object) this;
			var id = Registries.BLOCK_ENTITY_TYPE.getId(self);
			if (id == null) {
				return null;
			}

			this.lambdynlights$setting = new LightSourceSettingEntry("light_sources.settings.block_entities."
					+ id.getNamespace() + '.' + id.getPath().replace('/', '.'),
					true, null, null);
			LambDynLights.get().config.load(this.lambdynlights$setting);
		}

		return this.lambdynlights$setting;
	}

	@Override
	public Text lambdynlights$getName() {
		var self = (BlockEntityType<?>) (Object) this;
		var id = Registries.BLOCK_ENTITY_TYPE.getId(self);
		if (id == null) {
			return Text.empty();
		}
		return Text.literal(id.getNamespace() + ':' + id.getPath());
	}
}
