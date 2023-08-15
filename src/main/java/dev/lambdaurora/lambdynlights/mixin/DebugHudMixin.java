/*
 * Copyright � 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Adds a debug string for dynamic light sources tracking and updates.
 *
 * @author LambdAurora
 * @version 1.3.2
 * @since 1.3.2
 */
@Mixin(DebugHud.class)
public class DebugHudMixin {
	@Inject(method = "getLeftText", at = @At("RETURN"))
	private void onGetLeftText(CallbackInfoReturnable<List<String>> cir) {
		var list = cir.getReturnValue();
		var ldl = LambDynLights.get();
		var builder = new StringBuilder("Dynamic Light Sources: ");
		builder.append(ldl.getLightSourcesCount())
				.append(" (U: ")
				.append(ldl.getLastUpdateCount());

		if (!ldl.config.getDynamicLightsMode().isEnabled()) {
			builder.append(" ; ");
			builder.append(Formatting.RED);
			builder.append("Disabled");
			builder.append(Formatting.RESET);
		}

		builder.append(')');
		list.add(builder.toString());
	}
}
