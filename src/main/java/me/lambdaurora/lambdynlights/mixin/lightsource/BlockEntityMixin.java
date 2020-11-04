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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.WorldRenderer;
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

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements DynamicLightSource
{
    @Shadow
    protected BlockPos pos;

    @Shadow
    @Nullable
    protected World world;

    @Shadow
    protected boolean removed;
    private int lambdynlights_luminance = 0;
    private int lambdynlights_lastLuminance = 0;
    private long lambdynlights_lastUpdate = 0;
    private LongOpenHashSet trackedLitChunkPos = new LongOpenHashSet();

    @Override
    public double getDynamicLightX()
    {
        return this.pos.getX() + 0.5;
    }

    @Override
    public double getDynamicLightY()
    {
        return this.pos.getY() + 0.5;
    }

    @Override
    public double getDynamicLightZ()
    {
        return this.pos.getZ() + 0.5;
    }

    @Override
    public World getDynamicLightWorld()
    {
        return this.world;
    }

    @Inject(method = "markRemoved", at = @At("TAIL"))
    private void onRemoved(CallbackInfo ci)
    {
        this.setDynamicLightEnabled(false);
    }

    @Override
    public void resetDynamicLight()
    {
        this.lambdynlights_lastLuminance = 0;
    }

    @Override
    public void dynamicLightTick()
    {
        // We do not want to update the entity on the server.
        if (this.world == null || !this.world.isClient())
            return;
        if (!this.removed) {
            this.lambdynlights_luminance = DynamicLightHandlers.getLuminanceFrom((BlockEntity) (Object) this);
            LambDynLights.updateTracking(this);

            if (!this.isDynamicLightEnabled()) {
                this.lambdynlights_lastLuminance = 0;
            }
        }
    }

    @Override
    public int getLuminance()
    {
        return this.lambdynlights_luminance;
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
    public boolean lambdynlights_updateDynamicLight(@NotNull WorldRenderer renderer)
    {
        if (!this.shouldUpdateDynamicLight())
            return false;

        int luminance = this.getLuminance();

        if (luminance != this.lambdynlights_lastLuminance) {
            this.lambdynlights_lastLuminance = luminance;

            if (this.trackedLitChunkPos.isEmpty()) {
                BlockPos chunkPos = new BlockPos(MathHelper.floorDiv(this.pos.getX(), 16),
                        MathHelper.floorDiv(this.pos.getY(), 16),
                        MathHelper.floorDiv(this.pos.getZ(), 16));

                LambDynLights.updateTrackedChunks(chunkPos, null, this.trackedLitChunkPos);

                Direction directionX = (this.pos.getX() & 15) >= 8 ? Direction.EAST : Direction.WEST;
                Direction directionY = (this.pos.getY() & 15) >= 8 ? Direction.UP : Direction.DOWN;
                Direction directionZ = (this.pos.getZ() & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;

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
                    LambDynLights.updateTrackedChunks(chunkPos, null, this.trackedLitChunkPos);
                }
            }

            // Schedules the rebuild of chunks.
            this.lambdynlights_scheduleTrackedChunksRebuild(renderer);
            return true;
        }
        return false;
    }

    @Override
    public void lambdynlights_scheduleTrackedChunksRebuild(@NotNull WorldRenderer renderer)
    {
        for (long pos : this.trackedLitChunkPos) {
            LambDynLights.scheduleChunkRebuild(renderer, BlockPos.fromLong(pos));
        }
    }
}
