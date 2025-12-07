/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.source;

import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.engine.CellHasher;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import dev.lambdaurora.lambdynlights.engine.lookup.SpatialLookupDeferredEntry;
import dev.lambdaurora.lambdynlights.engine.lookup.SpatialLookupEntry;
import dev.lambdaurora.lambdynlights.engine.scheduler.ChunkRebuildStatus;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongConsumer;
import java.util.stream.Stream;

/**
 * Represents a dynamic light source which is deferred to a {@link DynamicLightBehavior}.
 *
 * @author LambdAurora, Akarys
 * @version 4.8.0
 * @since 4.0.0
 */
public final class DeferredDynamicLightSource implements DynamicLightSource {
	private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

	private final int id = ID_COUNTER.getAndIncrement();
	private final DynamicLightBehavior behavior;
	private DynamicLightBehavior.@Nullable BoundingBox previousBoundingBox;

	public DeferredDynamicLightSource(DynamicLightBehavior behavior) {
		this.behavior = behavior;
		this.previousBoundingBox = null;
	}

	public DynamicLightBehavior behavior() {
		return this.behavior;
	}

	@Override
	public int getDynamicLightId() {
		return this.id;
	}

	@Override
	public Stream<SpatialLookupEntry> splitIntoDynamicLightEntries(CellHasher cellHasher) {
		DynamicLightBehavior.BoundingBox boundingBox = this.behavior.getBoundingBox();

		var chunks = new ArrayList<SpatialLookupEntry>();

		int cellEndX = DynamicLightingEngine.positionToCell(boundingBox.endX());
		int cellEndY = DynamicLightingEngine.positionToCell(boundingBox.endY());
		int cellEndZ = DynamicLightingEngine.positionToCell(boundingBox.endZ());

		for (int x = DynamicLightingEngine.positionToCell(boundingBox.startX()); x <= cellEndX; x++) {
			for (int y = DynamicLightingEngine.positionToCell(boundingBox.startY()); y <= cellEndY; y++) {
				for (int z = DynamicLightingEngine.positionToCell(boundingBox.startZ()); z <= cellEndZ; z++) {
					chunks.add(new SpatialLookupDeferredEntry(cellHasher.hashCell(x, y, z), behavior));
				}
			}
		}

		return chunks.stream();
	}

	@Override
	public Long2ObjectMap<ChunkRebuildStatus> getDynamicLightChunksToRebuild(boolean forced) {
		if (!forced && !this.behavior.hasChanged()) {
			return Long2ObjectMaps.emptyMap();
		}

		DynamicLightBehavior.BoundingBox boundingBox = this.behavior.getBoundingBox();

		var chunks = new Long2ObjectOpenHashMap<ChunkRebuildStatus>();

		if (this.previousBoundingBox != null) {
			addBoundingBoxToChunksSet(this.previousBoundingBox, chunk -> chunks.put(chunk, ChunkRebuildStatus.REMOVE_REQUESTED));
		}
		addBoundingBoxToChunksSet(boundingBox, chunk -> chunks.put(chunk, ChunkRebuildStatus.REQUESTED));

		this.previousBoundingBox = boundingBox;

		return chunks;
	}

	private static void addBoundingBoxToChunksSet(
			DynamicLightBehavior.BoundingBox boundingBox, LongConsumer consumer
	) {
		int chunkStartX = getStartChunk(boundingBox.startX());
		int chunkStartY = getStartChunk(boundingBox.startY());
		int chunkStartZ = getStartChunk(boundingBox.startZ());
		int chunkEndX = getEndChunk(boundingBox.endX());
		int chunkEndY = getEndChunk(boundingBox.endY());
		int chunkEndZ = getEndChunk(boundingBox.endZ());

		for (int x = chunkStartX; x <= chunkEndX; x++) {
			for (int y = chunkStartY; y <= chunkEndY; y++) {
				for (int z = chunkStartZ; z <= chunkEndZ; z++) {
					consumer.accept(SectionPos.asLong(x, y, z));
				}
			}
		}
	}

	private static int getStartChunk(int blockPos) {
		int chunkStart = SectionPos.blockToSectionCoord(blockPos);

		if ((Mth.floor(blockPos) & 15) < 8) {
			chunkStart--;
		}

		return chunkStart;
	}

	private static int getEndChunk(int blockPos) {
		int chunkStart = SectionPos.blockToSectionCoord(blockPos);

		if ((Mth.floor(blockPos) & 15) >= 8) {
			chunkStart++;
		}

		return chunkStart;
	}
}
