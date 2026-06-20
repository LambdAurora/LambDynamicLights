/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.compat;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import dev.lambdaurora.lambdynlights.gui.SettingsScreen;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Provides the settings entry in Sodium's settings screen.
 *
 * @author LambdAurora
 * @version 4.9.0
 * @since 4.9.0
 */
public final class SodiumCompat implements ConfigEntryPoint {
	@Override
	public void registerConfigLate(ConfigBuilder builder) {
		builder.registerOwnModOptions()
				.setName(LambDynLightsConstants.MOD_CONTAINER.getName())
				.setColorTheme(builder.createColorTheme()
						.setBaseThemeRGB(0xffff5aa2)
				)
				.setNonTintedIcon(Identifier.parse("lambdynlights:icon.png"))
				.addPage(builder.createExternalPage()
						.setName(Component.translatable(
								"lambdynlights.menu.sodium.tab",
								Component.translatable(LambDynLights.INSTANCE.config.dynamicLightsModeOption.key)
						))
						.setScreenConsumer(screen -> Minecraft.getInstance().setScreenAndShow(new SettingsScreen(screen)))
				);
	}
}
