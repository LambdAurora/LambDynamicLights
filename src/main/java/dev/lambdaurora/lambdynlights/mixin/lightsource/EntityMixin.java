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
import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSourceBehavior;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityDynamicLightSourceBehavior {
	@Shadow
	private int id;

	@Shadow
	public abstract Level level();

	@Shadow
	public abstract double getX();

	@Shadow
	public abstract double getEyeY();

	@Shadow
	public abstract double getZ();

	@Shadow
	public abstract double getY();

	@Shadow
	public abstract boolean isOnFire();

	@Shadow
	public abstract boolean isCurrentlyGlowing();

	@Shadow
	public abstract boolean isInvisible();

	// Must be private, DO NOT TRUST INHERITANCE WHEN USING @UNIQUE
	@Unique
	private int lambdynlights$luminance = 0;
	@Unique
	private int lambdynlights$lastLuminance = 0;
	@Unique
	private double lambdynlights$prevX;
	@Unique
	private double lambdynlights$prevY;
	@Unique
	private double lambdynlights$prevZ;
	@Unique
	private LongSet lambdynlights$trackedLitChunkPos = LongSet.of();

	@Inject(method = "remove", at = @At("TAIL"))
	public void onRemove(CallbackInfo ci) {
		if (this.level().isClientSide())
			this.setDynamicLightEnabled(false);
	}

	@Override
	public int getDynamicLightId() {
		return this.id;
	}

	@Override
	public double getDynamicLightX() {
		return this.getX();
	}

	@Override
	public double getDynamicLightY() {
		return this.getEyeY();
	}

	@Override
	public double getDynamicLightZ() {
		return this.getZ();
	}

	@Override
	public double getDynamicLightPrevX() {
		return this.lambdynlights$prevX;
	}

	@Override
	public double getDynamicLightPrevY() {
		return this.lambdynlights$prevY;
	}

	@Override
	public double getDynamicLightPrevZ() {
		return this.lambdynlights$prevZ;
	}

	@Override
	public void updateDynamicLightPreviousCoordinates() {
		this.lambdynlights$prevX = this.getDynamicLightX();
		this.lambdynlights$prevY = this.getDynamicLightY();
		this.lambdynlights$prevZ = this.getDynamicLightZ();
	}

	@Override
	public void resetDynamicLight() {
		this.lambdynlights$lastLuminance = 0;
	}

	@Override
	public void dynamicLightTick() {
		if (this.isInvisible()) {
			this.lambdynlights$luminance = 0;
		} else {
			this.lambdynlights$luminance = this.isOnFire() ? 15 : 0;

			int luminance = LambDynLights.getLuminanceFrom((Entity) (Object) this);
			if (luminance > this.lambdynlights$luminance)
				this.lambdynlights$luminance = luminance;
		}
	}

	@Override
	public final int getLuminance() {
		return this.lambdynlights$luminance;
	}

	@Override
	public final void setLuminance(int luminance) {
		this.lambdynlights$luminance = luminance;
	}

	@Override
	public final int getLastDynamicLuminance() {
		return this.lambdynlights$lastLuminance;
	}

	@Override
	public final void setLastDynamicLuminance(int luminance) {
		this.lambdynlights$lastLuminance = luminance;
	}

	@Override
	public final LongSet lambdynlights$getTrackedLitChunkPos() {
		return this.lambdynlights$trackedLitChunkPos;
	}

	@Override
	public final void lambdynlights$setTrackedLitChunkPos(LongSet trackedLitChunkPos) {
		this.lambdynlights$trackedLitChunkPos = trackedLitChunkPos;
	}
}
