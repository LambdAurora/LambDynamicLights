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
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements DynamicLightSource {
    @Shadow
    public World world;

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
    public abstract EntityType<?> getType();

    @Shadow
    public abstract BlockPos getBlockPos();

    @Shadow
    public abstract boolean isRemoved();

    @Shadow
    public abstract ChunkPos getChunkPos();

    @Unique
    private int luminance = 0;
    @Unique
    private int lastLuminance = 0;
    @Unique
    private long lastUpdate = 0;
    @Unique
    private double prevX;
    @Unique
    private double prevY;
    @Unique
    private double prevZ;
    @Unique
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
                    this.luminance = 0;
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
        this.lastLuminance = 0;
    }

    @Override
    public boolean shouldUpdateDynamicLight() {
        var mode = LambDynLights.get().config.getDynamicLightsMode();
        if (!mode.isEnabled())
            return false;
        if (mode.hasDelay()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime < this.lastUpdate + mode.getDelay()) {
                return false;
            }

            this.lastUpdate = currentTime;
        }
        return true;
    }

    @Override
    public void dynamicLightTick() {
        this.luminance = this.isOnFire() ? 15 : 0;

        int luminance = DynamicLightHandlers.getLuminanceFrom((Entity) (Object) this);
        if (luminance > this.luminance)
            this.luminance = luminance;
    }

    @Override
    public int getLuminance() {
        return this.luminance;
    }

    @Override
    public boolean lambdynlights$updateDynamicLight(@NotNull WorldRenderer renderer) {
        if (!this.shouldUpdateDynamicLight())
            return false;
        double deltaX = this.getX() - this.prevX;
        double deltaY = this.getY() - this.prevY;
        double deltaZ = this.getZ() - this.prevZ;

        int luminance = this.getLuminance();

        if (Math.abs(deltaX) > 0.1D || Math.abs(deltaY) > 0.1D || Math.abs(deltaZ) > 0.1D || luminance != this.lastLuminance) {
            this.prevX = this.getX();
            this.prevY = this.getY();
            this.prevZ = this.getZ();
            this.lastLuminance = luminance;

            var newPos = new LongOpenHashSet();

            if (luminance > 0) {
                var entityChunkPos = this.getChunkPos();
                var chunkPos = new BlockPos.Mutable(entityChunkPos.x, ChunkSectionPos.getSectionCoord(this.getEyeY()), entityChunkPos.z);

                LambDynLights.scheduleChunkRebuild(renderer, chunkPos);
                LambDynLights.updateTrackedChunks(chunkPos, this.trackedLitChunkPos, newPos);

                var directionX = (this.getBlockPos().getX() & 15) >= 8 ? Direction.EAST : Direction.WEST;
                var directionY = (MathHelper.fastFloor(this.getEyeY()) & 15) >= 8 ? Direction.UP : Direction.DOWN;
                var directionZ = (this.getBlockPos().getZ() & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;

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
            this.lambdynlights$scheduleTrackedChunksRebuild(renderer);
            // Update tracked lit chunks.
            this.trackedLitChunkPos = newPos;
            return true;
        }
        return false;
    }

    @Override
    public void lambdynlights$scheduleTrackedChunksRebuild(@NotNull WorldRenderer renderer) {
        if (MinecraftClient.getInstance().world == this.world)
            for (long pos : this.trackedLitChunkPos) {
                LambDynLights.scheduleChunkRebuild(renderer, pos);
            }
    }
}
