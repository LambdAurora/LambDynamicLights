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
import dev.lambdaurora.spruceui.background.SimpleColorBackground;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import io.github.queerbric.pride.PrideClient;
import io.github.queerbric.pride.PrideFlag;
import io.github.queerbric.pride.PrideFlagShapes;
import io.github.queerbric.pride.PrideFlags;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

import java.util.Random;

/**
 * Displays a pride flag.
 * <p>
 * If you have an issue with this, I don't care.
 *
 * @author LambdAurora
 * @version 3.2.0
 * @since 2.1.0
 */
public class RandomPrideFlagBackground implements Background {
	private static final Background SECOND_LAYER = new SimpleColorBackground(0xe0101010);
	private static final Random RANDOM = new Random();

	private final PrideFlag flag;

	public RandomPrideFlagBackground(PrideFlag flag) {
		this.flag = flag;
	}

	@Override
	public void render(GuiGraphics graphics, SpruceWidget widget, int vOffset, int mouseX, int mouseY, float delta) {
		int x = widget.getX();
		int y = widget.getY();

		if (this.flag.getShape() == PrideFlagShapes.get(Identifier.of("pride", "horizontal_stripes"))) {
			graphics.drawSpecial(bufferSource -> {
				var buffer = bufferSource.getBuffer(PrideClient.FLAG_SHAPE_TRIANGLE_RENDER_TYPE);

				int width = widget.getWidth();
				int height = widget.getHeight();

				float partHeight = height / (this.flag.getColors().size() - 1.f);

				// First one
				float rightY = y;
				float leftY = y;

				int color = this.flag.getColors().getInt(0);
				buffer.addVertex(x + width, rightY + partHeight, 0).color(color);
				buffer.addVertex(x + width, rightY, 0).color(color);
				buffer.addVertex(x, leftY, 0).color(color);

				rightY += partHeight;

				for (int i = 1; i < this.flag.getColors().size() - 1; i++) {
					color = this.flag.getColors().getInt(i);

					buffer.addVertex(x + width, rightY + partHeight, 0).color(color);
					buffer.addVertex(x + width, rightY, 0).color(color);
					buffer.addVertex(x, leftY, 0).color(color);

					buffer.addVertex(x + width, rightY + partHeight, 0).color(color);
					buffer.addVertex(x, leftY, 0).color(color);
					buffer.addVertex(x, leftY + partHeight, 0).color(color);

					rightY += partHeight;
					leftY += partHeight;
				}

				// Last one
				color = this.flag.getColors().getInt(this.flag.getColors().size() - 1);
				buffer.addVertex(x + width, rightY, 0).color(color);
				buffer.addVertex(x, leftY, 0).color(color);
				buffer.addVertex(x, y + height, 0).color(color);
			});
		} else {
			this.flag.render(graphics, x, y, widget.getWidth(), widget.getHeight());
		}

		SECOND_LAYER.render(graphics, widget, vOffset, mouseX, mouseY, delta);
	}

	/**
	 * Returns a random pride flag as background.
	 *
	 * @return the background
	 */
	public static Background random() {
		return new RandomPrideFlagBackground(PrideFlags.getRandomFlag(RANDOM));
	}
}
