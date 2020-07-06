/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin;

import me.lambdaurora.lambdynlights.DynamicLightSource;
import me.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "me.jellysquid.mods.sodium.client.model.light.EntityLighter")
public class EntityLighterMixin
{
    @Shadow @Final private static double MAX_LIGHTMAP_COORD;

    @Shadow @Final private static double MAX_LIGHT_VAL;

    @Inject(method = "getBlendedLight", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    private static void onGetBlendedLight(Entity entity, float tickDelta, CallbackInfoReturnable<Integer> cir,
                                          boolean calcBlockLight,
                                          double x1, double y1, double z1,
                                          double x2, double y2, double z2,
                                          int bMinX, int bMinY, int bMinZ,
                                          int bMaxX, int bMaxY, int bMaxZ,
                                          double max,
                                          double sl, double bl,
                                          BlockPos.Mutable pos,
                                          int bli, int sli)
    {
        double posLuminance = LambDynLights.get().getDynamicLuminance(entity.getBlockPos());
        double entityLuminance = ((DynamicLightSource) entity).getLuminance();
        double dynamicLuminance = Math.max(posLuminance, entityLuminance);
        int sodiumLuminance = (int) Math.ceil((bl / max) * 15.0);
        if (dynamicLuminance > sodiumLuminance) {
            max = 0.0D;
            bl = 0.0;

            if (entityLuminance != dynamicLuminance) {
                // Iterate over every block in the sampling volume
                for (int bX = bMinX; bX <= bMaxX; bX++) {
                    for (int bY = bMinY; bY <= bMaxY; bY++) {
                        for (int bZ = bMinZ; bZ <= bMaxZ; bZ++) {
                            pos.set(bX, bY, bZ);

                            BlockState blockState = entity.world.getBlockState(pos);

                            // Do not consider light-blocking volumes
                            if (blockState.isOpaqueFullCube(entity.world, pos) && blockState.getLuminance() <= 0) {
                                continue;
                            }

                            // Find the intersecting volume between the entity box and the block's bounding box
                            double ix1 = Math.max(bX, x1);
                            double iy1 = Math.max(bY, y1);
                            double iz1 = Math.max(bZ, z1);
                            double ix2 = Math.min(bX + 1, x2);
                            double iy2 = Math.min(bY + 1, y2);
                            double iz2 = Math.min(bZ + 1, z2);

                            // The amount of light this block can contribute is the volume of the intersecting box
                            double weight = (ix2 - ix1) * (iy2 - iy1) * (iz2 - iz1);

                            // Keep count of how much light could've been contributed
                            max += weight;

                            if (calcBlockLight) {
                                bl += weight * (LambDynLights.get().getDynamicLuminance(pos) / MAX_LIGHT_VAL);
                            } else {
                                bl += weight;
                            }
                        }
                    }
                }
                cir.setReturnValue(((sli & 0xFFFF) << 16) | ((int) (bl / max * MAX_LIGHTMAP_COORD) & 0xFFFF));
            } else {
                cir.setReturnValue(((sli & 0xFFFF) << 16) | ((int) (dynamicLuminance / MAX_LIGHT_VAL * MAX_LIGHTMAP_COORD) & 0xFFFF));
            }
        }
    }
}
