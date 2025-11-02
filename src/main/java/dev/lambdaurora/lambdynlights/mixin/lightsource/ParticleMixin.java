/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.lightsource;

import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSourceBehavior;
import dev.lambdaurora.lambdynlights.engine.source.ParticleLightSourceBehavior;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(Particle.class)
public class ParticleMixin implements EntityDynamicLightSourceBehavior {
	@Shadow
	@Final
	protected ClientLevel level;
	@Shadow
	protected double x;
	@Shadow
	protected double y;
	@Shadow
	protected double z;

	@Unique
	private int lambdynlights$id = ParticleLightSourceBehavior.ID_COUNTER.getAndIncrement();
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

	@Override
	public int getDynamicLightId() {
		return this.lambdynlights$id;
	}

	@Override
	public double getDynamicLightX() {
		return this.x;
	}

	@Override
	public double getDynamicLightY() {
		return this.y;
	}

	@Override
	public double getDynamicLightZ() {
		return this.z;
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

	@Override
	public void dynamicLightTick() {
	}
}
