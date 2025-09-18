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
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents a debug renderer for dynamic lighting.
 *
 * @author LambdAurora, Akarys
 * @version 4.4.0
 * @since 4.0.0
 */
@Environment(EnvType.CLIENT)
public class DynamicLightSectionDebugRenderer extends DynamicLightDebugRenderer {
	private static final Duration REFRESH_INTERVAL = Duration.ofMillis(250);
	private static final int RADIUS = 10;
	private static final int COLOR = 0x3fff9900;
	private static final int ACTIVE_COLOR = 0x3fffff00;
	private static final int ACTIVE_NEIGHBOR_COLOR = 0x19ffbf00;
	private final DynamicLightingEngine lightingEngine;
	private Instant lastUpdateTime = Instant.now();

	@Nullable
	private DynamicLightSectionDebugRenderer.SectionData data;

	public DynamicLightSectionDebugRenderer(LambDynLights mod) {
		super(mod);
		this.lightingEngine = mod.engine;
	}

	@Override
	public void render(MatrixStack matrices, MultiBufferSource bufferSource, double x, double y, double z) {
		int cellDisplayRadius = this.config.getDebugCellDisplayRadius();

		if (!this.config.getDebugActiveDynamicLightingCells().get() && cellDisplayRadius == 0) {
			// Don't render debugging stuff if debug rendering is disabled.
			return;
		}

		Instant instant = Instant.now();
		if (this.data == null || Duration.between(this.lastUpdateTime, instant).compareTo(REFRESH_INTERVAL) > 0) {
			this.lastUpdateTime = instant;
			this.data = new DynamicLightSectionDebugRenderer.SectionData(
					this.lightingEngine, RADIUS, this.client.player.getBlockPos()
			);
		}

		int playerCellX = DynamicLightingEngine.positionToCell(this.client.player.getBlockPos().getX());
		int playerCellY = DynamicLightingEngine.positionToCell(this.client.player.getBlockPos().getY());
		int playerCellZ = DynamicLightingEngine.positionToCell(this.client.player.getBlockPos().getZ());

		int playerHash = this.lightingEngine.hashCell(playerCellX, playerCellY, playerCellZ);

		if (cellDisplayRadius > 0) {
			for (int offsetX = 0; offsetX < cellDisplayRadius * 2 + 1; offsetX++) {
				for (int offsetY = 0; offsetY < cellDisplayRadius * 2 + 1; offsetY++) {
					for (int offsetZ = 0; offsetZ < cellDisplayRadius * 2 + 1; offsetZ++) {
						int cellX = playerCellX + offsetX - cellDisplayRadius;
						int cellY = playerCellY + offsetY - cellDisplayRadius;
						int cellZ = playerCellZ + offsetZ - cellDisplayRadius;
						int currentHash = this.lightingEngine.hashCell(cellX, cellY, cellZ);

						DebugRenderer.renderFloatingText(
								matrices,
								bufferSource,
								"HASH(%d, %d, %d) = %d".formatted(cellX, cellY, cellZ, currentHash),
								cellX * DynamicLightingEngine.CELL_SIZE + DynamicLightingEngine.CELL_SIZE / 2.0,
								cellY * DynamicLightingEngine.CELL_SIZE + DynamicLightingEngine.CELL_SIZE / 2.0,
								cellZ * DynamicLightingEngine.CELL_SIZE + DynamicLightingEngine.CELL_SIZE / 2.0,
								(playerHash == currentHash) ? 0xffff0000 : 0xff00ff00,
								.08f
						);
					}
				}
			}
		}

		renderEdges(matrices, this.data.matchShape, this.data.origin, bufferSource, x, y, z, DynamicLightingEngine.CELL_SIZE, COLOR);
		if (this.config.getDebugActiveDynamicLightingCells().get()) {
			renderEdges(matrices, this.data.activeShape, this.data.origin, bufferSource, x, y, z, DynamicLightingEngine.CELL_SIZE, ACTIVE_COLOR);
			renderEdges(matrices, this.data.activeNeighborShape, this.data.origin, bufferSource, x, y, z, DynamicLightingEngine.CELL_SIZE, ACTIVE_NEIGHBOR_COLOR);

			this.data.activeShape.forAllBoxes((startCellX, startCellY, startCellZ, endCellX, endCellY, endCellZ) -> {
				for (int currentCellX = startCellX; currentCellX < endCellX; currentCellX++) {
					for (int currentCellY = startCellY; currentCellY < endCellY; currentCellY++) {
						for (int currentCellZ = startCellZ; currentCellZ < endCellZ; currentCellZ++) {
							int cellX = currentCellX + data.origin.getX();
							int cellY = currentCellY + data.origin.getY();
							int cellZ = currentCellZ + data.origin.getZ();

							DebugRenderer.renderFloatingText(
									matrices,
									bufferSource,
									"%d".formatted(this.lightingEngine.getEntryCountAt(cellX, cellY, cellZ)),
									cellX * DynamicLightingEngine.CELL_SIZE + DynamicLightingEngine.CELL_SIZE / 2.0,
									cellY * DynamicLightingEngine.CELL_SIZE + DynamicLightingEngine.CELL_SIZE / 2.0,
									cellZ * DynamicLightingEngine.CELL_SIZE + DynamicLightingEngine.CELL_SIZE / 2.0,
									0xffffff00,
									.2f
							);
						}
					}
				}
			}, true);
		}

		VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.debugSectionQuads());
		renderFaces(matrices, this.data.matchShape, this.data.origin, vertexConsumer, x, y, z, DynamicLightingEngine.CELL_SIZE, COLOR);
		if (this.config.getDebugActiveDynamicLightingCells().get()) {
			renderFaces(matrices, this.data.activeShape, this.data.origin, vertexConsumer, x, y, z, DynamicLightingEngine.CELL_SIZE, ACTIVE_COLOR);
			renderFaces(matrices, this.data.activeNeighborShape, this.data.origin, vertexConsumer, x, y, z, DynamicLightingEngine.CELL_SIZE, ACTIVE_NEIGHBOR_COLOR);
		}
	}

	@Environment(EnvType.CLIENT)
	static final class SectionData {
		final DiscreteVoxelShape matchShape;
		final DiscreteVoxelShape activeShape;
		final DiscreteVoxelShape activeNeighborShape;
		final int playerCellX;
		final int playerCellY;
		final int playerCellZ;
		final int playerHash;
		final Vec3i origin;

		SectionData(DynamicLightingEngine engine, int radius, BlockPos playerPos) {
			int perimeter = radius * 2 + 1;
			this.matchShape = new BitSetDiscreteVoxelShape(perimeter, perimeter, perimeter);
			this.activeShape = new BitSetDiscreteVoxelShape(perimeter, perimeter, perimeter);
			this.activeNeighborShape = new BitSetDiscreteVoxelShape(perimeter, perimeter, perimeter);

			this.playerCellX = DynamicLightingEngine.positionToCell(playerPos.getX());
			this.playerCellY = DynamicLightingEngine.positionToCell(playerPos.getY());
			this.playerCellZ = DynamicLightingEngine.positionToCell(playerPos.getZ());

			this.playerHash = engine.hashCell(this.playerCellX, this.playerCellY, this.playerCellZ);

			for (int offsetX = 0; offsetX < perimeter; offsetX++) {
				for (int offsetY = 0; offsetY < perimeter; offsetY++) {
					for (int offsetZ = 0; offsetZ < perimeter; offsetZ++) {
						int currentCellX = playerCellX - radius + offsetX;
						int currentCellY = playerCellY - radius + offsetY;
						int currentCellZ = playerCellZ - radius + offsetZ;

						if (engine.hashCell(currentCellX, currentCellY, currentCellZ) == this.playerHash
								&& ((offsetX != radius) || (offsetY != radius) || (offsetZ != radius))) {
							this.matchShape.fill(offsetX, offsetY, offsetZ);
						}

						if (engine.hasEntriesAt(currentCellX, currentCellY, currentCellZ)) {
							this.activeShape.fill(offsetX, offsetY, offsetZ);

							for (int neighorX = -1; neighorX <= 1; neighorX++) {
								for (int neighorY = -1; neighorY <= 1; neighorY++) {
									for (int neighorZ = -1; neighorZ <= 1; neighorZ++) {
										if (offsetX + neighorX >= 0 && offsetY + neighorY >= 0 && offsetZ + neighorZ >= 0
												&& offsetX + neighorX < perimeter && offsetY + neighorY < perimeter && offsetZ + neighorZ < perimeter) {
											this.activeNeighborShape.fill(offsetX + neighorX, offsetY + neighorY, offsetZ + neighorZ);
										}
									}
								}
							}
						}
					}
				}
			}

			this.origin = new Vec3i(
					this.playerCellX - radius,
					this.playerCellY - radius,
					this.playerCellZ - radius
			);
		}
	}
}
