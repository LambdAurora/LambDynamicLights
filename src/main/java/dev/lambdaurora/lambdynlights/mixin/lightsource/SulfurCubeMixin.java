/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.lightsource;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSource;
import dev.lambdaurora.lambdynlights.util.ExplosiveLuminance;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SulfurCube.class)
public abstract class SulfurCubeMixin extends EntityMixin implements EntityDynamicLightSource {
	@Shadow
	@Final
	private static EntityDataAccessor<Integer> MAX_FUSE;

	@Shadow
	public abstract int getFuse();

	@Override
	public boolean isDynamicLightEnabled() {
		return super.isDynamicLightEnabled() && LambDynLights.get().config.getTntLightingMode().isEnabled();
	}

	@Override
	public void dynamicLightTick() {
		ExplosiveLuminance.doExplosiveLuminanceTick(
				this, this::getFuse, () -> this.entityData.get(MAX_FUSE), super::dynamicLightTick
		);
	}
}
