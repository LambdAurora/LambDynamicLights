/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimedTnt.class)
public abstract class PrimedTntEntityMixin extends EntityMixin implements EntityDynamicLightSource {
	@Shadow
	public abstract int getFuse();

	@Unique
	private int startFuseTimer = 80;

	@Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
	private void onNew(EntityType<? extends PrimedTnt> type, Level level, CallbackInfo ci) {
		this.startFuseTimer = this.getFuse();
	}

	@Override
	public boolean isDynamicLightEnabled() {
		return super.isDynamicLightEnabled() && LambDynLights.get().config.getTntLightingMode().isEnabled();
	}

	@Override
	public void dynamicLightTick() {
		ExplosiveLuminance.doExplosiveLuminanceTick(
				this, this::getFuse, () -> this.startFuseTimer, super::dynamicLightTick
		);
	}
}
