/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin.lightsource;

import me.lambdaurora.lambdynlights.DynamicLightSource;
import me.lambdaurora.lambdynlights.DynamicLightsMode;
import me.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin implements DynamicLightSource
{
    @Shadow
    public World world;

    @Shadow
    public boolean removed;

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getEyeY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract double getY();

    @Shadow
    public double prevX;
    @Shadow
    public double prevY;
    @Shadow
    public double prevZ;

    @Shadow
    public int chunkX;
    @Shadow
    public int chunkZ;

    private int           lambdynlights_lastLuminance = 0;
    private long          lambdynlights_lastUpdate    = 0;
    private Set<BlockPos> trackedLitChunkPos          = new HashSet<>();

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTick(CallbackInfo ci)
    {
        if (this.world.isClient()) {
            // We do not want to update the entity on the server.
            if (this.removed) {
                this.setDynamicLightEnabled(false);
            } else {
                this.dynamicLightTick();
                LambDynLights.updateTracking(this);

                if (!this.isDynamicLightEnabled()) {
                    this.lambdynlights_lastLuminance = 0;
                }
            }
        }
    }

    @Inject(method = "remove", at = @At("TAIL"))
    public void onRemove(CallbackInfo ci)
    {
        if (this.world.isClient())
            this.setDynamicLightEnabled(false);
    }

    @Override
    public Entity getDynamicLightEntity()
    {
        return (Entity) (Object) this;
    }

    @Override
    public boolean shouldUpdateDynamicLight()
    {
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
    public void lambdynlights_updateDynamicLight(@NotNull WorldRenderer renderer)
    {
        if (!this.shouldUpdateDynamicLight())
            return;

        double deltaX = this.getX() - this.prevX;
        double deltaY = this.getY() - this.prevY;
        double deltaZ = this.getZ() - this.prevZ;

        int luminance = this.getLuminance();

        if (Math.abs(deltaX) > 0.1D || Math.abs(deltaY) > 0.1D || Math.abs(deltaZ) > 0.1D || luminance != this.lambdynlights_lastLuminance) {
            this.lambdynlights_lastLuminance = luminance;

            Set<BlockPos> newPos = new HashSet<>();

            if (luminance > 0) {
                BlockPos chunkPos = new BlockPos(this.chunkX, MathHelper.floorDiv((int) this.getEyeY(), 16), this.chunkZ);

                LambDynLights.scheduleChunkRebuild(renderer, chunkPos);
                LambDynLights.updateTrackedChunks(chunkPos, this.trackedLitChunkPos, newPos);

                Direction directionX = (MathHelper.fastFloor(this.getX()) & 15) >= 8 ? Direction.EAST : Direction.WEST;
                Direction directionY = (MathHelper.fastFloor(this.getEyeY()) & 15) >= 8 ? Direction.UP : Direction.DOWN;
                Direction directionZ = (MathHelper.fastFloor(this.getZ()) & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;

                for (int i = 0; i < 7; i++) {
                    if (i % 4 == 0) {
                        chunkPos = chunkPos.offset(directionX); // X
                    } else if (i % 4 == 1) {
                        chunkPos = chunkPos.offset(directionZ); // XZ
                    } else if (i % 4 == 2) {
                        chunkPos = chunkPos.offset(directionX.getOpposite()); // Z
                    } else {
                        chunkPos = chunkPos.offset(directionZ.getOpposite()); // origin
                        chunkPos = chunkPos.offset(directionY); // Y
                    }
                    LambDynLights.scheduleChunkRebuild(renderer, chunkPos);
                    LambDynLights.updateTrackedChunks(chunkPos, this.trackedLitChunkPos, newPos);
                }
            }

            // Schedules the rebuild of removed chunks.
            this.lambdynlights_scheduleTrackedChunksRebuild(renderer);
            // Update tracked lit chunks.
            this.trackedLitChunkPos = newPos;
        }
    }

    @Override
    public void lambdynlights_scheduleTrackedChunksRebuild(@NotNull WorldRenderer renderer)
    {
        for (BlockPos pos : this.trackedLitChunkPos) {
            LambDynLights.scheduleChunkRebuild(renderer, pos);
        }
    }
}
