/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lambdaurora.spruceui.background.Background;
import dev.lambdaurora.spruceui.background.SimpleColorBackground;
import dev.lambdaurora.spruceui.render.SpruceGuiGraphics;
import dev.lambdaurora.spruceui.util.ColorUtil;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import io.github.queerbric.pride.PrideFlag;
import io.github.queerbric.pride.PrideFlags;
import io.github.queerbric.pride.shape.HorizontalPrideFlagShape;
import io.github.queerbric.pride.shape.PrideFlagShape;
import io.github.queerbric.pride.shape.VerticalPrideFlagShape;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.Random;

/**
 * Displays a pride flag.
 * <p>
 * If you have an issue with this, I don't care.
 *
 * @author LambdAurora
 * @version 4.3.0
 * @since 2.1.0
 */
public class RandomPrideFlagBackground implements Background {
	private static final Background SECOND_LAYER = new SimpleColorBackground(0xd0101010);
	private static final HorizontalPrideFlagShape DEFAULT_RAINBOW = new HorizontalPrideFlagShape(IntList.of(
			0xffff0018, 0xffffa52c, 0xffffff41, 0xff008018, 0xff0000f9, 0xff86007d
	));
	private static final Random RANDOM = new Random();

	private final PrideFlag flag;
	private final boolean nuhUh;

	RandomPrideFlagBackground(PrideFlag flag, boolean nuhUh) {
		this.flag = flag;
		this.nuhUh = nuhUh;
	}

	private PrideFlagShape getShape() {
		return this.nuhUh ? DEFAULT_RAINBOW : this.flag.getShape();
	}

	@Override
	public void render(SpruceGuiGraphics graphics, SpruceWidget widget, int vOffset, int mouseX, int mouseY, float delta) {
		int x = widget.getX();
		int y = widget.getY();
		int width = widget.getWidth();
		int height = widget.getHeight();

		if (this.getShape() instanceof HorizontalPrideFlagShape(var colors)) {
			graphics.submitGuiElement(new SlantedPrideFlagRenderState(
					RenderPipelines.GUI, TextureSetup.noTexture(),
					graphics.pose(),
					x, y, width, height,
					colors,
					null
			));
		} else {
			this.flag.render(graphics.vanilla(), x, y, widget.getWidth(), widget.getHeight());
		}

		SECOND_LAYER.render(graphics, widget, vOffset, mouseX, mouseY, delta);

		if (this.nuhUh) {
			var text = Text.literal("Nuh uh, you're not going to remove this, try harder :3c");
			var font = Minecraft.getInstance().font;
			var lines = font.wrapLines(text, width - 8);

			int startY = y + height - 24 - lines.size() * (font.lineHeight + 2);

			for (var line : lines) {
				graphics.drawCenteredShadowedText(font, line, x + width / 2, startY, 0xffff0000);
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
		boolean nuhUh = flag == null || areColorsSpoofed(flag);

		return new RandomPrideFlagBackground(flag, nuhUh);
	}

	private static boolean areColorsSpoofed(PrideFlag flag) {
		return switch (flag.getShape()) {
			case HorizontalPrideFlagShape(var colors) -> areColorsSpoofed(colors);
			case VerticalPrideFlagShape(var colors) -> areColorsSpoofed(colors);
			default -> false;
		};
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

	public record SlantedPrideFlagRenderState(
			RenderPipeline pipeline, TextureSetup textureSetup,
			Matrix3x2f pose,
			int x, int y,
			int width, int height,
			IntList colors,
			@Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds
	) implements GuiElementRenderState {
		public SlantedPrideFlagRenderState(
				RenderPipeline pipeline, TextureSetup textureSetup,
				Matrix3x2f pose,
				int x, int y,
				int width, int height,
				IntList colors,
				@Nullable ScreenRectangle scissorArea
		) {
			this(
					pipeline, textureSetup,
					pose,
					x, y,
					width, height,
					colors,
					scissorArea, getBounds(x, y, width, height, pose, scissorArea)
			);
		}

		@Override
		public void buildVertices(VertexConsumer vertexConsumer, float z) {
			float partHeight = this.height / (this.colors.size() - 1.f);

			// First one
			float rightY = this.y;
			float leftY = this.y;

			int color = this.colors.getInt(0);
			vertexConsumer.addVertex(x + width, rightY + partHeight, z).color(color);
			vertexConsumer.addVertex(x + width, rightY, z).color(color);
			vertexConsumer.addVertex(x, leftY, z).color(color);
			// Dirty 4th vertex as GUI only accepts quads.
			vertexConsumer.addVertex(x, leftY, z).color(color);

			rightY += partHeight;

			for (int i = 1; i < this.colors.size() - 1; i++) {
				color = this.colors.getInt(i);

				vertexConsumer.addVertex(x + width, rightY + partHeight, z).color(color);
				vertexConsumer.addVertex(x + width, rightY, z).color(color);
				vertexConsumer.addVertex(x, leftY, z).color(color);
				vertexConsumer.addVertex(x, leftY + partHeight, z).color(color);

				rightY += partHeight;
				leftY += partHeight;
			}

			// Last one
			color = this.colors.getInt(this.colors.size() - 1);
			vertexConsumer.addVertex(x + width, rightY, z).color(color);
			vertexConsumer.addVertex(x, leftY, z).color(color);
			vertexConsumer.addVertex(x, y + height, z).color(color);
			// Dirty 4th vertex as GUI only accepts quads.
			vertexConsumer.addVertex(x, y + height, z).color(color);
		}

		private static @Nullable ScreenRectangle getBounds(
				int x, int y, int width, int height, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea
		) {
			var defaultBounds = new ScreenRectangle(x, y, width, height)
					.transformMaxBounds(pose);
			return scissorArea != null ? scissorArea.intersection(defaultBounds) : defaultBounds;
		}
	}
}
