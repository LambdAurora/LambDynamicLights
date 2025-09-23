/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.platform.neoforge;

import dev.lambdaurora.lambdynlights.gui.SettingsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Represents the config screen provider for NeoForge.
 *
 * @author LambdAurora
 * @version 4.5.0
 * @since 4.5.0
 */
public final class NeoForgeConfigScreenProvider implements IConfigScreenFactory {
	static final NeoForgeConfigScreenProvider INSTANCE = new NeoForgeConfigScreenProvider();

	private NeoForgeConfigScreenProvider() {}

	@Override
	public Screen createScreen(ModContainer container, Screen modListScreen) {
		return new SettingsScreen(modListScreen);
	}
}
