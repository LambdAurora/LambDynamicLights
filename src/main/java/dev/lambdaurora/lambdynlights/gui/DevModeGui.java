/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.gui;

import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import dev.lambdaurora.spruceui.event.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Predicate;

/**
 * Represents the development-mode GUI overlay.
 *
 * @author LambdAurora
 * @version 4.4.0
 * @since 4.0.0
 */
public final class DevModeGui {
	private DevModeGui() {
		throw new UnsupportedOperationException("DevModeGui only contains static definitions.");
	}

	public static void init() {
		if (!LambDynLightsConstants.isDevMode()) {
			return;
		}

		ScreenEvents.AFTER_RENDER.register(
				(screen, graphics, mouseX, mouseY, tickDelta) -> render(graphics.vanilla()),
				Predicate.not(SettingsScreen.class::isInstance)
		);
	}

	public static void render(GuiGraphics graphics) {
		var client = Minecraft.getInstance();

		if (client.isGameLoadFinished() && !client.getDebugOverlay().showDebugScreen()) {
			int bottom = client.getWindow().getGuiScaledHeight();
			int y = bottom - client.font.lineHeight;

			graphics.fill(
					0, y - 4,
					client.font.width(LambDynLightsConstants.DEV_MODE_OVERLAY_TEXT) + 4, bottom,
					0xaa000000
			);
			graphics.drawString(client.font, LambDynLightsConstants.DEV_MODE_OVERLAY_TEXT, 2, y - 2, 0xffff0000);
		}
	}
}
