package me.lambdaurora.lambdynlights.mixin;

/*import grondag.canvas.render.CanvasFrustum;
import grondag.canvas.render.CanvasWorldRenderer;
import me.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*/
//@Mixin(CanvasWorldRenderer.class)
public class CanvasWorldRendererMixin
{
    /*@Inject(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lgrondag/canvas/terrain/RenderRegionBuilder;setCameraPosition(Lnet/minecraft/util/math/Vec3d;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onSetupTerrain(Camera camera, CanvasFrustum frustum, int frameCounter, boolean isSpectator, CallbackInfo ci)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        client.getProfiler().swap("dynamic_lighting");
        LambDynLights.get().updateAll(client.worldRenderer);
    }*/
}
