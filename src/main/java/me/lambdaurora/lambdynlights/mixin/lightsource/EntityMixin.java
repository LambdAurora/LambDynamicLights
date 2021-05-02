/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin.lightsource;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.lambdaurora.lambdynlights.DynamicLightSource;
import me.lambdaurora.lambdynlights.DynamicLightsMode;
import me.lambdaurora.lambdynlights.LambDynLights;
import me.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements DynamicLightSource {
    @Shadow
    public World world;

    @Shadow
    public boolean isRemoved() {
        return false;
    }

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getEyeY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract double getY();

    @Shadow
    public ChunkPos getChunkPos() {
        return null;
    }

    @Shadow
    public abstract boolean isOnFire();

    @Shadow
    public abstract EntityType<?> getType();

    @Shadow
    public abstract BlockPos getBlockPos();

    private int lambdynlights_luminance = 0;
    private int lambdynlights_lastLuminance = 0;
    private long lambdynlights_lastUpdate = 0;
    private double lambdynlights_prevX;
    private double lambdynlights_prevY;
    private double lambdynlights_prevZ;
    private LongOpenHashSet trackedLitChunkPos = new LongOpenHashSet();

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTick(CallbackInfo ci) {
        // We do not want to update the entity on the server.
        if (this.world.isClient()) {
            if (this.isRemoved()) {
                this.setDynamicLightEnabled(false);
            } else {
                this.dynamicLightTick();
                if (!LambDynLights.get().config.hasEntitiesLightSource() && this.getType() != EntityType.PLAYER)
                    this.lambdynlights_luminance = 0;
                LambDynLights.updateTracking(this);
            }
        }
    }

    @Inject(method = "remove", at = @At("TAIL"))
    public void onRemove(CallbackInfo ci) {
        if (this.world.isClient())
            this.setDynamicLightEnabled(false);
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
    public World getDynamicLightWorld() {
        return this.world;
    }

    @Override
    public void resetDynamicLight() {
        this.lambdynlights_lastLuminance = 0;
    }

    @Override
    public boolean shouldUpdateDynamicLight() {
        DynamicLightsMode mode = LambDynLights.get().config.getDynamicLightsMode();
        if (!mode.isEnabled())
            return false;
        if (mode.hasDelay()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime < this.lambdynlights_lastUpdate + mode.getDelay()) {
                return false;
            }

            this.lambdynlights_lastUpdate = currentTime;
        }
        return true;
    }

    @Override
    public void dynamicLightTick() {
        this.lambdynlights_luminance = this.isOnFire() ? 15 : 0;

        int luminance = DynamicLightHandlers.getLuminanceFrom((Entity) (Object) this);
        if (luminance > this.lambdynlights_luminance)
            this.lambdynlights_luminance = luminance;
    }

    @Override
    public int getLuminance() {
        return this.lambdynlights_luminance;
    }

    @Override
    public boolean lambdynlights_updateDynamicLight(@NotNull WorldRenderer renderer) {
        if (!this.shouldUpdateDynamicLight())
            return false;
        double deltaX = this.getX() - this.lambdynlights_prevX;
        double deltaY = this.getY() - this.lambdynlights_prevY;
        double deltaZ = this.getZ() - this.lambdynlights_prevZ;

        int luminance = this.getLuminance();

        if (Math.abs(deltaX) > 0.1D || Math.abs(deltaY) > 0.1D || Math.abs(deltaZ) > 0.1D || luminance != this.lambdynlights_lastLuminance) {
            this.lambdynlights_prevX = this.getX();
            this.lambdynlights_prevY = this.getY();
            this.lambdynlights_prevZ = this.getZ();
            this.lambdynlights_lastLuminance = luminance;

            LongOpenHashSet newPos = new LongOpenHashSet();

            if (luminance > 0) {
                BlockPos.Mutable chunkPos = new BlockPos.Mutable(this.getChunkPos().x, MathHelper.floorDiv((int) this.getEyeY(), 16), this.getChunkPos().z);

                LambDynLights.scheduleChunkRebuild(renderer, chunkPos);
                LambDynLights.updateTrackedChunks(chunkPos, this.trackedLitChunkPos, newPos);

                Direction directionX = (this.getBlockPos().getX() & 15) >= 8 ? Direction.EAST : Direction.WEST;
                Direction directionY = (MathHelper.fastFloor(this.getEyeY()) & 15) >= 8 ? Direction.UP : Direction.DOWN;
                Direction directionZ = (this.getBlockPos().getZ() & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;

                for (int i = 0; i < 7; i++) {
                    if (i % 4 == 0) {
                        chunkPos.move(directionX); // X
                    } else if (i % 4 == 1) {
                        chunkPos.move(directionZ); // XZ
                    } else if (i % 4 == 2) {
                        chunkPos.move(directionX.getOpposite()); // Z
                    } else {
                        chunkPos.move(directionZ.getOpposite()); // origin
                        chunkPos.move(directionY); // Y
                    }
                    LambDynLights.scheduleChunkRebuild(renderer, chunkPos);
                    LambDynLights.updateTrackedChunks(chunkPos, this.trackedLitChunkPos, newPos);
                }
            }

            // Schedules the rebuild of removed chunks.
            this.lambdynlights_scheduleTrackedChunksRebuild(renderer);
            // Update tracked lit chunks.
            this.trackedLitChunkPos = newPos;
            return true;
        }
        return false;
    }

    @Override
    public void lambdynlights_scheduleTrackedChunksRebuild(@NotNull WorldRenderer renderer) {
        if (MinecraftClient.getInstance().world == this.world)
            for (long pos : this.trackedLitChunkPos) {
                LambDynLights.scheduleChunkRebuild(renderer, pos);
            }
    }
}
