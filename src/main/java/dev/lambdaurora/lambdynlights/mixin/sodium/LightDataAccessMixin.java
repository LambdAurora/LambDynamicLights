package dev.lambdaurora.lambdynlights.mixin.sodium;

import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.model.light.data.LightDataAccess")
public class LightDataAccessMixin {
    // In Sodium 0.5, LightDataAccess has replaced the Vanilla LightmapTextureManager,
    // and no longer uses WorldRenderer#getLightmapCoordinates.
    // Unfortunately, this breaks LambDynLights, and as a result is forced to mixin to Sodium.
    @Dynamic
    @Redirect(method = "compute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/BlockRenderView;getLightLevel(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)I", ordinal = 0), remap = false)
    private int lambdynlights$modifyBlockLight(BlockRenderView world, LightType type, BlockPos pos, int x, int y, int z) {
        int original = world.getLightLevel(type, pos);

        if (!world.getBlockState(pos).isOpaqueFullCube(world, pos) && LambDynLights.get().config.getDynamicLightsMode().isEnabled()) {
            int dynamic = (int) LambDynLights.get().getDynamicLightLevel(pos);
            if (dynamic > original)
                return dynamic;
        }

        return original;
    }
}
