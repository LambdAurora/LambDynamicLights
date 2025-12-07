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
import dev.lambdaurora.spruceui.util.ColorUtil;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import io.github.queerbric.pride.*;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Random;

/**
 * Displays a pride flag.
 * <p>
 * If you have an issue with this, I don't care.
 *
 * @author LambdAurora
 * @version 4.0.2
 * @since 2.1.0
 */
public class RandomPrideFlagBackground implements Background {
	private static final Background SECOND_LAYER = new SimpleColorBackground(0xe0101010);
	private static final IntList DEFAULT_RAINBOW_COLORS = IntList.of(
			0xffff0018, 0xffffa52c, 0xffffff41, 0xff008018, 0xff0000f9, 0xff86007d
	);
	private static final PrideFlagShape PROGRESS = PrideFlagShapes.get(Identifier.fromNamespaceAndPath("pride", "progress"));
	private static final PrideFlagShape HORIZONTAL_STRIPES
			= PrideFlagShapes.get(Identifier.fromNamespaceAndPath("pride", "horizontal_stripes"));
	private static final Random RANDOM = new Random();

	private final PrideFlag flag;
	private final boolean nuhUh;

	RandomPrideFlagBackground(PrideFlag flag, boolean nuhUh) {
		this.flag = flag;
		this.nuhUh = nuhUh;
	}

	private IntList getColors() {
		return this.nuhUh ? DEFAULT_RAINBOW_COLORS : this.flag.getColors();
	}

	@Override
	public void render(GuiGraphics graphics, SpruceWidget widget, int vOffset, int mouseX, int mouseY, float delta) {
		int x = widget.getX();
		int y = widget.getY();
		int width = widget.getWidth();
		int height = widget.getHeight();

		if (this.nuhUh || this.flag.getShape() == HORIZONTAL_STRIPES) {
			graphics.drawSpecial(bufferSource -> {
				var buffer = bufferSource.getBuffer(PrideClient.FLAG_SHAPE_TRIANGLE_RENDER_TYPE);

				var colors = this.getColors();

				float partHeight = height / (colors.size() - 1.f);

				// First one
				float rightY = y;
				float leftY = y;

				int color = colors.getInt(0);
				buffer.addVertex(x + width, rightY + partHeight, 0).setColor(color);
				buffer.addVertex(x + width, rightY, 0).setColor(color);
				buffer.addVertex(x, leftY, 0).setColor(color);

				rightY += partHeight;

				for (int i = 1; i < colors.size() - 1; i++) {
					color = colors.getInt(i);

					buffer.addVertex(x + width, rightY + partHeight, 0).setColor(color);
					buffer.addVertex(x + width, rightY, 0).setColor(color);
					buffer.addVertex(x, leftY, 0).setColor(color);

					buffer.addVertex(x + width, rightY + partHeight, 0).setColor(color);
					buffer.addVertex(x, leftY, 0).setColor(color);
					buffer.addVertex(x, leftY + partHeight, 0).setColor(color);

					rightY += partHeight;
					leftY += partHeight;
				}

				// Last one
				color = colors.getInt(colors.size() - 1);
				buffer.addVertex(x + width, rightY, 0).setColor(color);
				buffer.addVertex(x, leftY, 0).setColor(color);
				buffer.addVertex(x, y + height, 0).setColor(color);
			});
		} else {
			this.flag.render(graphics, x, y, widget.getWidth(), widget.getHeight());
		}

		SECOND_LAYER.render(graphics, widget, vOffset, mouseX, mouseY, delta);

		if (this.nuhUh) {
			var text = Component.literal("Nuh uh, you're not going to remove this, try harder :3c");
			var font = Minecraft.getInstance().font;
			var lines = font.split(text, width - 8);

			int startY = y + height - 24 - lines.size() * (font.lineHeight + 2);

			for (var line : lines) {
				graphics.drawCenteredString(font, line, x + width / 2, startY, 0xffff0000);
				startY += font.lineHeight + 2;
			}
		}
	}

	/**
	 * Returns a random pride flag as background.
	 *
	 * @return the background
	 */
	public static Background random() {
		var flag = PrideFlags.getRandomFlag(RANDOM);
		boolean nuhUh = flag == null || (flag.getShape() != PROGRESS && areColorsSpoofed(flag.getColors()));

		return new RandomPrideFlagBackground(flag, nuhUh);
	}

	private static boolean areColorsSpoofed(IntList colors) {
		if (colors.size() < 2) {
			return true;
		} else {
			int maxDist = 0;

			for (int colorA : colors) {
				for (int colorB : colors) {
					int dist = colorDist(colorA, colorB);

					if (dist > maxDist) {
						maxDist = dist;
					}
				}
			}

			return maxDist < 10;
		}
	}

	private static int colorDist(int a, int b) {
		// https://en.wikipedia.org/wiki/Color_difference#sRGB
		float r = (ColorUtil.argbUnpackRed(a) + ColorUtil.argbUnpackRed(b)) / 2.f;
		int deltaR = ColorUtil.argbUnpackRed(a) - ColorUtil.argbUnpackRed(b);
		int deltaG = ColorUtil.argbUnpackGreen(a) - ColorUtil.argbUnpackGreen(b);
		int deltaB = ColorUtil.argbUnpackBlue(a) - ColorUtil.argbUnpackBlue(b);

		return (int) Math.sqrt((2 + r / 256.f) * deltaR * deltaR + 4 * deltaG * deltaG + (2 + (255 - r) / 256) * deltaB * deltaB);
	}
}
