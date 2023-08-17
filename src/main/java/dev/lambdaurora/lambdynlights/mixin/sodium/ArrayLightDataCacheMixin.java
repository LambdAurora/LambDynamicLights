/*
 * Copyright © 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.sodium;

import dev.lambdaurora.lambdynlights.LambDynLights;
import me.jellysquid.mods.sodium.client.model.light.data.LightDataAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.model.light.data.ArrayLightDataCache", remap = false)
public abstract class ArrayLightDataCacheMixin extends LightDataAccess {
    @Unique
    private final BlockPos.Mutable lambdynlights$pos = new BlockPos.Mutable();

    // In Sodium 0.5, LightDataAccess has replaced the Vanilla LightmapTextureManager,
    // and no longer uses WorldRenderer#getLightmapCoordinates.
    // Unfortunately, this breaks LambDynLights, and as a result is forced to mixin to Sodium.
    @Dynamic
    @Inject(method = "get(III)I", at = @At("RETURN"), cancellable = true)
    private void lambdynlights$modifyBlockLight(int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        this.lambdynlights$pos.set(x, y, z);

        BlockRenderView world = this.world;

        // Use Sodium's provided unpack method in case the format ever changes.
        int packed = cir.getReturnValueI();
        int original = LightDataAccess.unpackBL(packed);

        if (!world.getBlockState(this.lambdynlights$pos).isOpaqueFullCube(world, this.lambdynlights$pos) && LambDynLights.get().config.getDynamicLightsMode().isEnabled()) {
            int dynamic = (int) LambDynLights.get().getDynamicLightLevel(this.lambdynlights$pos);
            if (dynamic > original) {
                // Remove the original block light value from the packed value,
                // then add the dynamic packed value in.
                // TODO: maybe the repacking of the original can be avoided somehow?
                cir.setReturnValue((packed & ~(LightDataAccess.packBL(original))) | LightDataAccess.packBL(dynamic));
            }
        }
    }
}
