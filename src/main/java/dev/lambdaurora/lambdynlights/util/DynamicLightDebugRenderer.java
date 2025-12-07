/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lambdaurora.lambdynlights.DynamicLightsConfig;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.engine.scheduler.ChunkRebuildStatus;
import dev.lambdaurora.spruceui.util.ColorUtil;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Represents a debug renderer for dynamic lighting.
 *
 * @author LambdAurora
 * @version 4.8.0
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
			PoseStack poses,
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
			renderFace(poses, vertexConsumer, direction, x, y, z, cellSize, realCellX, realCellY, realCellZ, color);
		});
	}

	static void renderEdges(
			PoseStack poses,
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
					poses, vertexConsumer, x, y, z, cellSize,
					realStartCellX, realStartCellY, realStartCellZ,
					realEndCellX, realEndCellY, realEndCellZ,
					color
			);
		}, true);
	}

	static void renderFace(
			PoseStack poses, VertexConsumer vertexConsumer, Direction direction,
			double x, double y, double z,
			int cellSize, int cellX, int cellY, int cellZ,
			int color
	) {
		float faceX = (float) (cellX * cellSize - x);
		float faceY = (float) (cellY * cellSize - y);
		float faceZ = (float) (cellZ * cellSize - z);
		ShapeRenderer.renderFace(
				poses.last().pose(), vertexConsumer, direction,
				faceX, faceY, faceZ,
				faceX + cellSize, faceY + cellSize, faceZ + cellSize,
				ColorUtil.floatColor(ColorUtil.argbUnpackRed(color)),
				ColorUtil.floatColor(ColorUtil.argbUnpackGreen(color)),
				ColorUtil.floatColor(ColorUtil.argbUnpackBlue(color)),
				ColorUtil.floatColor(ColorUtil.argbUnpackAlpha(color))
		);
	}

	static void renderEdge(
			PoseStack poses, VertexConsumer vertexConsumer,
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
		renderLine(poses, vertexConsumer, startX, startY, startZ, endX, endY, endZ, color);
	}

	static void renderLine(
			PoseStack poses, VertexConsumer vertexConsumer,
			float startX, float startY, float startZ,
			float endX, float endY, float endZ,
			int color
	) {
		Matrix4f modelMatrix = poses.last().pose();
		vertexConsumer.addVertex(modelMatrix, startX, startY, startZ).setColor(color);
		vertexConsumer.addVertex(modelMatrix, endX, endY, endZ).setColor(color);
	}

	public static class SectionRebuild extends DynamicLightDebugRenderer {
		private static final int SCHEDULED_COLOR = 0x3f0099ff;
		private static final int REQUESTED_COLOR = 0x3f9b00a6;
		private final Long2IntMap scheduledChunks = new Long2IntOpenHashMap();
		private @Nullable Long2ObjectMap<int[]> requestedChunks;

		public SectionRebuild(LambDynLights mod) {
			super(mod);
		}

		public boolean isEnabled() {
			return this.config.getDebugDisplayDynamicLightingChunkRebuilds().get();
		}

		@Override
		public void render(
				PoseStack poses, MultiBufferSource bufferSource, double x, double y, double z,
				DebugValueAccess debugValueAccess, Frustum frustum
		) {
			if (!this.isEnabled()) return;

			poses.pushPose();
			poses.translate(-x, -y, -z);
			for (var entry : this.scheduledChunks.long2IntEntrySet()) {
				this.renderBox(poses, bufferSource, SCHEDULED_COLOR, entry.getIntValue() / 4.f, SectionPos.of(entry.getLongKey()));
			}
			poses.popPose();

			if (this.requestedChunks != null) {
				for (var chunk : this.requestedChunks.long2ObjectEntrySet()) {
					var chunkPos = SectionPos.of(chunk.getLongKey());
					var statuses = chunk.getValue();
					boolean canRenderBox = false;

					int statusY = chunkPos.maxBlockY() - 4;
					for (int i = 0; i < statuses.length; i++) {
						if (statuses[i] > 0) {
							var status = ChunkRebuildStatus.VALUES.get(i);

							DebugRenderer.renderFloatingText(
									poses,
									bufferSource,
									statuses[i] + "x " + status,
									chunkPos.minBlockX() + 8,
									statusY,
									chunkPos.minBlockZ() + 8,
									status.color(),
									.08f
							);

							if (i != ChunkRebuildStatus.AFFECTED.ordinal()) {
								canRenderBox = true;
							}
						}

						statusY -= 2;
					}

					if (canRenderBox) {
						poses.pushPose();
						poses.translate(-x, -y, -z);
						this.renderBox(poses, bufferSource, REQUESTED_COLOR, 1.f, chunkPos);
						poses.popPose();
					}
				}
			}
		}

		private void renderBox(PoseStack poses, MultiBufferSource bufferSource, int color, float alpha, SectionPos chunk) {
			float red = ColorUtil.floatColor(ColorUtil.argbUnpackRed(color));
			float green = ColorUtil.floatColor(ColorUtil.argbUnpackGreen(color));
			float blue = ColorUtil.floatColor(ColorUtil.argbUnpackBlue(color));

			ShapeRenderer.renderLineBox(
					poses.last(), bufferSource.getBuffer(RenderType.lines()),
					chunk.minBlockX(), chunk.minBlockY(), chunk.minBlockZ(),
					SectionPos.sectionToBlockCoord(chunk.x(), 16), SectionPos.sectionToBlockCoord(chunk.y(), 16), SectionPos.sectionToBlockCoord(chunk.z(), 16),
					red, green, blue, alpha
			);
		}

		public void scheduleChunkRebuild(long chunkPos) {
			if (!this.isEnabled()) return;

			this.scheduledChunks.put(chunkPos, 4);
		}

		public void setRequestedChunks(Supplier<Long2ObjectMap<int[]>> chunks) {
			if (!this.isEnabled()) return;

			this.requestedChunks = chunks.get();
		}

		public void clearRequestedChunks() {
			this.requestedChunks = Long2ObjectMaps.emptyMap();
		}

		public void tick() {
			if (!this.isEnabled()) return;

			var iterator = this.scheduledChunks.long2IntEntrySet().iterator();
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
