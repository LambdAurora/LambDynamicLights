/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.source;

import dev.lambdaurora.lambdynlights.engine.CellHasher;
import dev.lambdaurora.lambdynlights.engine.lookup.SpatialLookupEntry;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.ChunkSectionPos;
import net.minecraft.core.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Represents a dynamic light source.
 *
 * @author LambdAurora
 * @version 4.4.0
 * @since 4.0.0
 */
public interface DynamicLightSource {
	/**
	 * Splits this dynamic light source into spatial lookup entries.
	 *
	 * @param cellHasher the cell hasher
	 * @return a stream of spatial lookup entries this source is made of
	 */
	Stream<SpatialLookupEntry> splitIntoDynamicLightEntries(@NotNull CellHasher cellHasher);

	/**
	 * Computes the set of chunk sections to rebuild to display in-world the new light values.
	 *
	 * @param forced {@code true} if relevant chunk sections must be returned to rebuild the chunk sections (for example if the light source is removed),
	 * or {@code false} otherwise
	 * @return the set of chunk sections to rebuild, or an empty set if none is to be rebuilt
	 */
	LongSet getDynamicLightChunksToRebuild(boolean forced);

	/**
	 * Gathers the closest chunks from the given coordinates.
	 *
	 * @param chunks the chunk set to add relevant chunks to
	 * @param x the X-coordinate
	 * @param y the Y-coordinate
	 * @param z the Z-coordinate
	 */
	static void gatherClosestChunks(LongSet chunks, double x, double y, double z) {
		var chunkPos = new BlockPos.Mutable(
				ChunkSectionPos.blockToSectionCoord(x),
				ChunkSectionPos.blockToSectionCoord(y),
				ChunkSectionPos.blockToSectionCoord(z)
		);

		chunks.add(ChunkSectionPos.asLong(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ()));

		var directionX = (MathHelper.floor(x) & 15) >= 8 ? Direction.EAST : Direction.WEST;
		var directionY = (MathHelper.floor(y) & 15) >= 8 ? Direction.UP : Direction.DOWN;
		var directionZ = (MathHelper.floor(z) & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;

		for (int i = 0; i < 7; i++) {
			if (i % 4 == 0) {
				chunkPos.move(directionX); // X
			} else if (i % 4 == 1) {
				chunkPos.move(directionZ); // XZ
			} else if (i % 4 == 2) {
				chunkPos.move(directionX.getOpposite()); // Z
			} else {
				chunkPos.move(directionZ.getOpposite()); // origin
				chunkPos.move(directionY); // Y
			}
			chunks.add(ChunkSectionPos.asLong(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ()));
		}
	}
}
