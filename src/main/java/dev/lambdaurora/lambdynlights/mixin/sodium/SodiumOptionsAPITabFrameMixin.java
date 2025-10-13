/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.sodium;

import dev.lambdaurora.lambdynlights.gui.SettingsScreen;
import dev.lambdaurora.lambdynlights.util.SodiumOptionPage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Pseudo
@Mixin(targets = "toni.sodiumoptionsapi.gui.SodiumOptionsTabFrame", remap = false)
public class SodiumOptionsAPITabFrameMixin {
	@SuppressWarnings("ReferenceToMixin")
	@Dynamic
	@Inject(
			method = "setTab(Lme/flashyreese/mods/reeses_sodium_options/client/gui/frame/tab/Tab;)V",
			at = @At("HEAD"),
			remap = false,
			cancellable = true
	)
	public void lambdynlights$onSetTab(@Coerce Object tab, CallbackInfo ci) {
		if (((RSOTabAccessor) tab).getTitle().equals(SodiumOptionPage.TITLE)) {
			var client = Minecraft.getInstance();

			client.setScreen(new SettingsScreen(client.screen));
			ci.cancel();
		}
	}
}
