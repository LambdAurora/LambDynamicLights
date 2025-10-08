/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.scheduler;

import dev.lambdaurora.lambdynlights.engine.source.DynamicLightSource;
import dev.lambdaurora.lambdynlights.mixin.LevelRendererAccessor;
import dev.lambdaurora.lambdynlights.util.DynamicLightDebugRenderer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.ChunkSectionPos;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.function.Consumer;

/**
 * Represents a chunk section rebuild scheduler for dynamic light sources.
 *
 * @author LambdAurora, Akarys
 * @version 4.8.0
 * @since 4.8.0
 */
public abstract class ChunkRebuildScheduler implements Closeable {
	protected final Minecraft client = Minecraft.getInstance();
	protected final DynamicLightDebugRenderer.SectionRebuild sectionRebuildDebugRenderer;
	private int sourceUpdatedLastTick = 0;

	protected ChunkRebuildScheduler(DynamicLightDebugRenderer.SectionRebuild sectionRebuildDebugRenderer) {
		this.sectionRebuildDebugRenderer = sectionRebuildDebugRenderer;
	}

	/**
	 * {@return the last number of dynamic light source updates}
	 */
	public int getSourceUpdatedLastTick() {
		return this.sourceUpdatedLastTick;
	}

	public void appendF3Debug(@NotNull Consumer<String> consumer) {
	}

	public final void update(
			@NotNull DynamicLightSource lightSource, @NotNull Long2ObjectMap<ChunkRebuildStatus> chunks
	) {
		if (!chunks.isEmpty()) {
			this.sourceUpdatedLastTick++;
			this.accept(lightSource, chunks);
		}
	}

	protected abstract void accept(
			@NotNull DynamicLightSource lightSource, @NotNull Long2ObjectMap<ChunkRebuildStatus> chunks
	);

	public final void remove(@NotNull DynamicLightSource lightSource) {
		final var chunks = lightSource.getDynamicLightChunksToRebuild(true);

		if (!chunks.isEmpty()) {
			this.sourceUpdatedLastTick++;
		}

		this.remove(lightSource, chunks.keySet());
	}

	protected abstract void remove(@NotNull DynamicLightSource lightSource, @NotNull LongSet chunks);

	/**
	 * Closes this chunk rebuild scheduler.
	 */
	@Override
	public void close() {}

	public void startTick() {
		this.sourceUpdatedLastTick = 0;
	}

	public void endTick() {}

	/**
	 * Schedules a chunk rebuild at the specified chunk position.
	 *
	 * @param chunkPos the packed chunk position
	 */
	protected final void scheduleChunkRebuild(long chunkPos) {
		this.scheduleChunkRebuild(ChunkSectionPos.x(chunkPos), ChunkSectionPos.y(chunkPos), ChunkSectionPos.z(chunkPos));
		this.sectionRebuildDebugRenderer.scheduleChunkRebuild(chunkPos);
	}

	private void scheduleChunkRebuild(int x, int y, int z) {
		if (this.client.level != null) {
			((LevelRendererAccessor) this.client.levelRenderer).lambdynlights$scheduleChunkRebuild(x, y, z, false);
		}
	}
}
