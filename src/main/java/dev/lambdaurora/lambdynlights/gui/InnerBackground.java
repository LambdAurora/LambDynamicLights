/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.gui;

import dev.lambdaurora.spruceui.background.Background;
import dev.lambdaurora.spruceui.background.DirtTexturedBackground;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class InnerBackground implements Background {

	@Override
	public void render(DrawContext drawContext, SpruceWidget widget, int vOffset, int mouseX, int mouseY, float delta) {
		if (MinecraftClient.getInstance().world != null) {
			drawContext.fillGradient(widget.getX(), widget.getY(),
					widget.getX() + widget.getWidth(), widget.getY() + widget.getHeight(),
					0xc0060606, 0xd0060606);
		} else {
			DirtTexturedBackground.DARKENED.render(drawContext, widget, vOffset, mouseX, mouseY, delta);
		}
	}
}
