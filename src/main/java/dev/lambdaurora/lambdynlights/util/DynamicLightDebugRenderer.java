/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.util;

import com.mojang.blaze3d.vertex.MatrixStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lambdaurora.lambdynlights.DynamicLightsConfig;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.spruceui.util.ColorUtil;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.ChunkSectionPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

/**
 * Represents a debug renderer for dynamic lighting.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 4.0.0
 */
@Environment(EnvType.CLIENT)
public abstract class DynamicLightDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	final Minecraft client = Minecraft.getInstance();
	final DynamicLightsConfig config;

	public DynamicLightDebugRenderer(LambDynLights mod) {
		this.config = mod.config;
	}

	static void renderFaces(
			MatrixStack matrices,
			DiscreteVoxelShape shape,
			Vec3i origin,
			VertexConsumer vertexConsumer,
			double x,
			double y,
			double z,
			int cellSize,
			int color
	) {
		shape.forAllFaces((direction, cellX, cellY, cellZ) -> {
			int realCellX = cellX + origin.getX();
			int realCellY = cellY + origin.getY();
			int realCellZ = cellZ + origin.getZ();
			renderFace(matrices, vertexConsumer, direction, x, y, z, cellSize, realCellX, realCellY, realCellZ, color);
		});
	}

	static void renderEdges(
			MatrixStack matrices,
			DiscreteVoxelShape shape,
			Vec3i origin,
			MultiBufferSource multiBufferSource,
			double x, double y, double z,
			int cellSize,
			int color
	) {
		shape.forAllEdges((startCellX, startCellY, startCellZ, endCellX, endCellY, endCellZ) -> {
			int realStartCellX = startCellX + origin.getX();
			int realStartCellY = startCellY + origin.getY();
			int realStartCellZ = startCellZ + origin.getZ();
			int realEndCellX = endCellX + origin.getX();
			int realEndCellY = endCellY + origin.getY();
			int realEndCellZ = endCellZ + origin.getZ();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(1.0));
			renderEdge(
					matrices, vertexConsumer, x, y, z, cellSize,
					realStartCellX, realStartCellY, realStartCellZ,
					realEndCellX, realEndCellY, realEndCellZ,
					color
			);
		}, true);
	}

	static void renderFace(
			MatrixStack matrices, VertexConsumer vertexConsumer, Direction direction,
			double x, double y, double z,
			int cellSize, int cellX, int cellY, int cellZ,
			int color
	) {
		float faceX = (float) (cellX * cellSize - x);
		float faceY = (float) (cellY * cellSize - y);
		float faceZ = (float) (cellZ * cellSize - z);
		ShapeRenderer.renderFace(
				matrices, vertexConsumer, direction,
				faceX, faceY, faceZ,
				faceX + cellSize, faceY + cellSize, faceZ + cellSize,
				ColorUtil.floatColor(ColorUtil.argbUnpackRed(color)),
				ColorUtil.floatColor(ColorUtil.argbUnpackGreen(color)),
				ColorUtil.floatColor(ColorUtil.argbUnpackBlue(color)),
				ColorUtil.floatColor(ColorUtil.argbUnpackAlpha(color))
		);
	}

	static void renderEdge(
			MatrixStack matrices, VertexConsumer vertexConsumer,
			double x, double y, double z,
			int cellSize,
			int startCellX, int startCellY, int startCellZ,
			int endCellX, int endCellY, int endCellZ,
			int color
	) {
		float startX = (float) (startCellX * cellSize - x);
		float startY = (float) (startCellY * cellSize - y);
		float startZ = (float) (startCellZ * cellSize - z);
		float endX = (float) (endCellX * cellSize - x);
		float endY = (float) (endCellY * cellSize - y);
		float endZ = (float) (endCellZ * cellSize - z);
		renderLine(matrices, vertexConsumer, startX, startY, startZ, endX, endY, endZ, color);
	}

	static void renderLine(
			MatrixStack matrices, VertexConsumer vertexConsumer,
			float startX, float startY, float startZ,
			float endX, float endY, float endZ,
			int color
	) {
		Matrix4f modelMatrix = matrices.peek().model();
		vertexConsumer.addVertex(modelMatrix, startX, startY, startZ).color(color);
		vertexConsumer.addVertex(modelMatrix, endX, endY, endZ).color(color);
	}

	public static class SectionRebuild extends DynamicLightDebugRenderer {
		private static final int COLOR = 0x3f0099ff;
		private final Long2IntMap chunks = new Long2IntOpenHashMap();

		public SectionRebuild(LambDynLights mod) {
			super(mod);
		}

		private boolean isEnabled() {
			return this.config.getDebugDisplayDynamicLightingChunkRebuilds().get();
		}

		@Override
		public void render(@NotNull MatrixStack matrices, @NotNull MultiBufferSource bufferSource, double x, double y, double z) {
			if (!this.isEnabled()) return;

			matrices.push();
			matrices.translate(-x, -y, -z);
			for (var entry : this.chunks.long2IntEntrySet()) {
				var chunk = ChunkSectionPos.of(entry.getLongKey());

				float red = ColorUtil.floatColor(ColorUtil.argbUnpackRed(COLOR));
				float green = ColorUtil.floatColor(ColorUtil.argbUnpackGreen(COLOR));
				float blue = ColorUtil.floatColor(ColorUtil.argbUnpackBlue(COLOR));
				float alpha = entry.getIntValue() / 4.f;

				ShapeRenderer.renderLineBox(
						matrices, bufferSource.getBuffer(RenderType.lines()),
						chunk.minBlockX(), chunk.minBlockY(), chunk.minBlockZ(),
						ChunkSectionPos.sectionToBlockCoord(chunk.x(), 16), ChunkSectionPos.sectionToBlockCoord(chunk.y(), 16), ChunkSectionPos.sectionToBlockCoord(chunk.z(), 16),
						red, green, blue, alpha
				);
			}
			matrices.pop();
		}

		public void scheduleChunkRebuild(long chunkPos) {
			if (!this.isEnabled()) return;

			this.chunks.put(chunkPos, 4);
		}

		public void tick() {
			if (!this.isEnabled()) return;

			var iterator = this.chunks.long2IntEntrySet().iterator();
			while (iterator.hasNext()) {
				var entry = iterator.next();

				if (entry.getIntValue() == 0) {
					iterator.remove();
				} else {
					entry.setValue(entry.getIntValue() - 1);
				}
			}
		}
	}
}
