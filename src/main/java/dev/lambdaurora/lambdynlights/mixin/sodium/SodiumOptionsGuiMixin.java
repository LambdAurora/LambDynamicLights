/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.sodium;

import dev.lambdaurora.lambdynlights.gui.SettingsScreen;
import dev.lambdaurora.lambdynlights.util.SodiumOptionPage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Text;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gui.SodiumOptionsGUI", remap = false)
public class SodiumOptionsGuiMixin extends Screen {
	@Shadow(remap = false)
	@Final
	private List<Object> pages;

	@Unique
	private Object lambDynLights;

	protected SodiumOptionsGuiMixin(Text title) {
		super(title);
	}

	@Dynamic
	@Inject(method = "<init>", at = @At("RETURN"))
	private void lambdynlights$onInit(Screen prevScreen, CallbackInfo ci) {
		pages.add(this.lambDynLights = SodiumOptionPage.makeSodiumOptionPage(Text.literal("LambDynamicLights")));
	}

	@Dynamic
	@Inject(method = "setPage", at = @At("HEAD"), remap = false, cancellable = true)
	private void lambdynlights$onSetPage(@Coerce Object page, CallbackInfo ci) {
		if (page == this.lambDynLights) {
			this.client.setScreen(new SettingsScreen(this));
			ci.cancel();
		}
	}
}
