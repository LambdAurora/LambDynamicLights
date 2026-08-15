/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.entity.luminance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import org.jetbrains.annotations.Range;

/**
 * Provides a luminance value depending on the squish of a cube mob entity.
 *
 * @param lowSquishLuminance the luminance to provide under the given squish threshold
 * @param highSquishLuminance the luminance to provide above the given squish threshold
 * @param threshold the squish threshold
 * @author LambdAurora
 * @version 4.11.0
 * @since 4.11.0
 */
public record CubeMobSquishLuminance(
		EntityLuminance lowSquishLuminance,
		EntityLuminance highSquishLuminance,
		float threshold
) implements EntityLuminance {
	public static final MapCodec<CubeMobSquishLuminance> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					EntityLuminance.CODEC.fieldOf("low").forGetter(CubeMobSquishLuminance::lowSquishLuminance),
					EntityLuminance.CODEC.fieldOf("high").forGetter(CubeMobSquishLuminance::highSquishLuminance),
					Codec.FLOAT.fieldOf("threshold").forGetter(CubeMobSquishLuminance::threshold)
			).apply(instance, CubeMobSquishLuminance::new)
	);

	@Override
	public Type type() {
		return Type.CUBE_MOB_SQUISH;
	}

	@Override
	public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
		if (entity instanceof Slime slime) {
			if (slime.squish > threshold) return this.highSquishLuminance.getLuminance(itemLightSourceManager, entity);
			else return this.lowSquishLuminance.getLuminance(itemLightSourceManager, entity);
		}

		return 0;
	}
}
