/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.scheduler;

import dev.lambdaurora.lambdynlights.accessor.FrustumStorage;
import dev.lambdaurora.lambdynlights.engine.source.DynamicLightSource;
import dev.lambdaurora.lambdynlights.util.DynamicLightDebugRenderer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.ChunkSectionPos;
import org.jetbrains.annotations.NotNull;
import org.joml.FrustumIntersection;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a chunk section rebuild scheduler which will attempt to minimize chunk rebuilds thanks to frustum culling.
 *
 * @author LambdAurora, Akarys
 * @version 4.8.0
 * @since 4.8.0
 */
public final class CullingChunkRebuildScheduler extends ChunkRebuildScheduler {
	private final Long2ObjectMap<Map<DynamicLightSource, ChunkRebuildStatus>> trackedChunks
			= new Long2ObjectOpenHashMap<>();

	private final long[] times = new long[40];
	private int rebuildQueuedLastTick = 0;

	public CullingChunkRebuildScheduler(
			DynamicLightDebugRenderer.SectionRebuild sectionRebuildDebugRenderer
	) {
		super(sectionRebuildDebugRenderer);
	}

	public int getRebuildQueuedLastTick() {
		return this.rebuildQueuedLastTick;
	}

	/**
	 * {@return the number of currently queued rebuilds}
	 */
	public long getCurrentlyQueued() {
		return this.trackedChunks.long2ObjectEntrySet().stream()
				.filter(trackedChunk -> trackedChunk.getValue().values()
						.stream()
						.anyMatch(status -> status != ChunkRebuildStatus.AFFECTED)
				)
				.count();
	}

	/**
	 * {@return the average time it took in nanoseconds to compute spatial lookup across 40 ticks}
	 */
	public float getTickTime() {
		return (float) Arrays.stream(this.times)
				.filter(value -> value > 0)
				.average()
				.orElse(0);
	}

	@Override
	public void appendF3Debug(@NotNull Consumer<String> consumer) {
		consumer.accept("Scheduled Chunk Rebuilds (Culling): %d / %d | Timing: %.3fms (avg. 40t)"
				.formatted(
						this.getRebuildQueuedLastTick(), this.getCurrentlyQueued(), this.getTickTime() / 1_000_000.f
				)
		);
	}

	@Override
	public void accept(@NotNull DynamicLightSource lightSource, @NotNull Long2ObjectMap<ChunkRebuildStatus> chunks) {
		if (!chunks.isEmpty()) {
			for (var chunk : chunks.long2ObjectEntrySet()) {
				final var newStatus = chunk.getValue();
				var map = this.trackedChunks.get(chunk.getLongKey());

				// Early discard if we want to clean the chunk, but it's already cleaned.
				if (newStatus == ChunkRebuildStatus.REMOVE_REQUESTED && map == null) continue;

				ChunkRebuildStatus oldStatus = null;
				if (map == null) {
					map = new Object2ObjectOpenHashMap<>();
				} else {
					oldStatus = map.get(lightSource);
				}

				if (newStatus == ChunkRebuildStatus.REMOVE_REQUESTED && (oldStatus == null || !oldStatus.needsCleanup())) {
					map.remove(lightSource);
				} else {
					map.put(
							lightSource,
							(oldStatus != null && oldStatus.needsCleanup() && newStatus != ChunkRebuildStatus.REMOVE_REQUESTED)
									? ChunkRebuildStatus.REQUESTED_AGAIN
									: newStatus
					);
				}

				if (map.isEmpty()) {
					this.trackedChunks.remove(chunk.getLongKey());
				} else {
					this.trackedChunks.put(chunk.getLongKey(), map);
				}
			}
		}
	}

	@Override
	public void remove(@NotNull DynamicLightSource lightSource, @NotNull LongSet chunks) {
		for (long chunk : chunks) {
			var map = this.trackedChunks.get(chunk);

			if (map != null) {
				var oldStatus = map.get(lightSource);

				// If the chunks were only just requested and never got dirty, then we can clean it up.
				if (oldStatus == ChunkRebuildStatus.REQUESTED) {
					map.remove(lightSource);
				} else {
					map.put(lightSource, ChunkRebuildStatus.REMOVE_REQUESTED);
				}

				// Cleanup in case only this dynamic light source affected this chunk.
				if (map.isEmpty()) {
					this.trackedChunks.remove(chunk);
				}
			}
			// If the chunks were never tracked, then we don't even need to do anything!
		}
	}

	@Override
	public void close() {
		this.sectionRebuildDebugRenderer.clearRequestedChunks();
	}

	@Override
	public void startTick() {
		super.startTick();
		this.rebuildQueuedLastTick = 0;
	}

	@Override
	public void endTick() {
		long startTime = System.nanoTime();
		final var renderer = Minecraft.getInstance().levelRenderer;
		final var frustum = this.getFrustum(renderer);

		var chunkIt = this.trackedChunks.long2ObjectEntrySet().iterator();
		while (chunkIt.hasNext()) {
			final var entry = chunkIt.next();
			final long chunkPos = entry.getLongKey();
			var statusMap = entry.getValue();

			boolean shouldRebuild = false;
			for (var status : statusMap.values()) {
				shouldRebuild = status.needsRebuild();
				if (shouldRebuild) break;
			}

			if (shouldRebuild) {
				int x = ChunkSectionPos.x(chunkPos);
				int y = ChunkSectionPos.y(chunkPos);
				int z = ChunkSectionPos.z(chunkPos);

				int hitResult = frustum.cubeInFrustum(
						ChunkSectionPos.sectionToBlockCoord(x),
						ChunkSectionPos.sectionToBlockCoord(y),
						ChunkSectionPos.sectionToBlockCoord(z),
						ChunkSectionPos.sectionToBlockCoord(x, 15),
						ChunkSectionPos.sectionToBlockCoord(y, 15),
						ChunkSectionPos.sectionToBlockCoord(z, 15)
				);
				boolean isNotCulled = hitResult == FrustumIntersection.INTERSECT || hitResult == FrustumIntersection.INSIDE;

				if (isNotCulled) {
					var it = statusMap.entrySet().iterator();
					while (it.hasNext()) {
						var statusEntry = it.next();
						if (statusEntry.getValue() == ChunkRebuildStatus.REMOVE_REQUESTED) {
							it.remove();
						} else if (statusEntry.getValue().needsRebuild()) {
							statusMap.put(statusEntry.getKey(), ChunkRebuildStatus.AFFECTED);
						}
					}

					if (statusMap.isEmpty()) {
						chunkIt.remove();
					}

					this.scheduleChunkRebuild(chunkPos);
					this.rebuildQueuedLastTick++;
				}
			}
		}

		long endTime = System.nanoTime();
		for (int i = 0; i < this.times.length - 1; i++) {
			this.times[i] = this.times[i + 1];
		}
		this.times[this.times.length - 1] = endTime - startTime;

		this.sectionRebuildDebugRenderer.setRequestedChunks(() -> {
			var chunks = new Long2ObjectOpenHashMap<int[]>();
			this.trackedChunks.long2ObjectEntrySet()
					.forEach(trackedChunk -> {
						var statuses = new int[ChunkRebuildStatus.VALUES.size()];
						trackedChunk.getValue().values().forEach(status -> {
							statuses[status.ordinal()] += 1;
						});
						chunks.put(trackedChunk.getLongKey(), statuses);
					});
			return chunks;
		});
	}

	private Frustum getFrustum(LevelRenderer renderer) {
		return ((FrustumStorage) renderer).lambdynlights$getFrustum();
	}
}
