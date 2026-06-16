/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.entity.luminance;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Range;

/**
 * Provides the luminance value emitted by the given block state.
 *
 * @param state the block state to get the luminance value from
 * @author LambdAurora
 * @version 4.11.0
 * @since 4.11.0
 */
public record BlockLuminance(BlockState state) implements EntityLuminance {
	public static final MapCodec<BlockLuminance> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					BlockState.CODEC.fieldOf("state").forGetter(BlockLuminance::state)
			).apply(instance, BlockLuminance::new)
	);

	@Override
	public Type type() {
		return Type.BLOCK;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		return this.state.getLightEmission();
	}
}
