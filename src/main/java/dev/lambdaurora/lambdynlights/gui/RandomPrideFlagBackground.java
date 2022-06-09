/*
 * Copyright © 2020-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import dev.lambdaurora.spruceui.background.Background;
import dev.lambdaurora.spruceui.background.SimpleColorBackground;
import dev.lambdaurora.spruceui.util.ColorUtil;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import io.github.queerbric.pride.PrideFlag;
import io.github.queerbric.pride.PrideFlagShapes;
import io.github.queerbric.pride.PrideFlags;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Random;

/**
 * Displays a pride flag.
 * <p>
 * If you have an issue with this, I don't care.
 *
 * @author LambdAurora
 * @version 2.1.0
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
	public void render(MatrixStack matrices, SpruceWidget widget, int vOffset, int mouseX, int mouseY, float delta) {
		int x = widget.getX();
		int y = widget.getY();

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		if (this.flag.getShape() == PrideFlagShapes.get(new Identifier("pride", "horizontal_stripes"))) {
			RenderSystem.disableTexture();
			var model = matrices.peek().getPosition();
			var tessellator = Tessellator.getInstance();
			var vertices = tessellator.getBufferBuilder();
			vertices.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

			int width = widget.getWidth();
			int height = widget.getHeight();

			float partHeight = height / (this.flag.getColors().size() - 1.f);

			// First one
			float rightY = y;
			float leftY = y;

			int[] color = ColorUtil.unpackARGBColor(this.flag.getColors().getInt(0));
			vertices.vertex(model, x + width, rightY + partHeight, 0).color(color[0], color[1], color[2], color[3]).next();
			vertices.vertex(model, x + width, rightY, 0).color(color[0], color[1], color[2], color[3]).next();
			vertices.vertex(model, x, leftY, 0).color(color[0], color[1], color[2], color[3]).next();

			rightY += partHeight;

			for (int i = 1; i < this.flag.getColors().size() - 1; i++) {
				color = ColorUtil.unpackARGBColor(this.flag.getColors().getInt(i));

				vertices.vertex(model, x + width, rightY + partHeight, 0).color(color[0], color[1], color[2], color[3]).next();
				vertices.vertex(model, x + width, rightY, 0).color(color[0], color[1], color[2], color[3]).next();
				vertices.vertex(model, x, leftY, 0).color(color[0], color[1], color[2], color[3]).next();

				vertices.vertex(model, x + width, rightY + partHeight, 0).color(color[0], color[1], color[2], color[3]).next();
				vertices.vertex(model, x, leftY, 0).color(color[0], color[1], color[2], color[3]).next();
				vertices.vertex(model, x, leftY + partHeight, 0).color(color[0], color[1], color[2], color[3]).next();

				rightY += partHeight;
				leftY += partHeight;
			}

			// Last one
			color = ColorUtil.unpackARGBColor(this.flag.getColors().getInt(this.flag.getColors().size() - 1));
			vertices.vertex(model, x + width, rightY, 0).color(color[0], color[1], color[2], color[3]).next();
			vertices.vertex(model, x, leftY, 0).color(color[0], color[1], color[2], color[3]).next();
			vertices.vertex(model, x, y + height, 0).color(color[0], color[1], color[2], color[3]).next();

			tessellator.draw();
			RenderSystem.enableTexture();
		} else {
			this.flag.render(matrices, x, y, widget.getWidth(), widget.getHeight());
		}

		SECOND_LAYER.render(matrices, widget, vOffset, mouseX, mouseY, delta);
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
