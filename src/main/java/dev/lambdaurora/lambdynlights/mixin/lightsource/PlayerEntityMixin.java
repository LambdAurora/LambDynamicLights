/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.lightsource;

import dev.lambdaurora.lambdynlights.DynamicLightSource;
import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DynamicLightSource {
	@Shadow
	public abstract boolean isSpectator();

	@Unique
	private int luminance;
	@Unique
	private World lastWorld;

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public void dynamicLightTick() {
		if (this.isOnFire() || this.isGlowing()) {
			this.luminance = 15;
		} else {
			int luminance = 0;
			var eyePos = new BlockPos(this.getX(), this.getEyeY(), this.getZ());
			boolean submergedInFluid = !this.world.getFluidState(eyePos).isEmpty();
			for (var equipped : this.getItemsEquipped()) {
				if (!equipped.isEmpty())
					luminance = Math.max(luminance, LambDynLights.getLuminanceFromItemStack(equipped, submergedInFluid));
			}

			this.luminance = luminance;
		}

		if (this.isSpectator())
			this.luminance = 0;

		if (this.lastWorld != this.getEntityWorld()) {
			this.lastWorld = this.getEntityWorld();
			this.luminance = 0;
		}
	}

	@Override
	public int getLuminance() {
		return this.luminance;
	}
}
