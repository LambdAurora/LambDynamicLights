/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.lightsource;

import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin implements EntityDynamicLightSource {
	@Shadow
	public abstract boolean isSpectator();

	@Unique
	private Level lambdynlights$lastWorld;

	@Override
	public void dynamicLightTick() {
		if (this.isSpectator()) {
			this.setLuminance(0);
		} else {
			super.dynamicLightTick();
		}

		if (this.lambdynlights$lastWorld != this.level()) {
			this.lambdynlights$lastWorld = this.level();
			this.setLuminance(0);
		}
	}
}
