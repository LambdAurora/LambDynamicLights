/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lambdaurora.lambdynlights.gui.DevModeGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class DevModeMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@WrapOperation(
			method = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V")
	)
	private void handler(GuiGraphics graphics, Operation<Void> original) {
		if (this.minecraft.isGameLoadFinished() && !this.minecraft.getDebugOverlay().showDebugScreen()) {
			DevModeGui.render(graphics);
		}
		original.call(graphics);
	}
}
