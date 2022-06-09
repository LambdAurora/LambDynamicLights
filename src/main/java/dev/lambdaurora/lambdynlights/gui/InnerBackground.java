/*
 * Copyright © 2020-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.gui;

import dev.lambdaurora.spruceui.background.Background;
import dev.lambdaurora.spruceui.background.DirtTexturedBackground;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public class InnerBackground extends DrawableHelper implements Background {
	@Override
	public void render(MatrixStack matrices, SpruceWidget widget, int vOffset, int mouseX, int mouseY, float delta) {
		if (MinecraftClient.getInstance().world != null) {
			this.fillGradient(matrices, widget.getX(), widget.getY(),
					widget.getX() + widget.getWidth(), widget.getY() + widget.getHeight(),
					0xc0060606, 0xd0060606);
		} else {
			DirtTexturedBackground.DARKENED.render(matrices, widget, vOffset, mouseX, mouseY, delta);
		}
	}
}
