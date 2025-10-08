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
import dev.lambdaurora.lambdynlights.util.DynamicLightDebugRenderer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Represents an immediate chunk section rebuild scheduler which will always schedule when requested.
 *
 * @author LambdAurora
 * @version 4.8.0
 * @since 4.8.0
 */
public final class SimpleChunkRebuildScheduler extends ChunkRebuildScheduler {
	private int rebuildRequestedLastTick = 0;

	public SimpleChunkRebuildScheduler(DynamicLightDebugRenderer.SectionRebuild sectionRebuildDebugRenderer) {
		super(sectionRebuildDebugRenderer);
	}

	@Override
	public void appendF3Debug(@NotNull Consumer<String> consumer) {
		consumer.accept("Scheduled Chunk Rebuilds (Immediate): %d".formatted(this.rebuildRequestedLastTick));
	}

	@Override
	protected void accept(@NotNull DynamicLightSource lightSource, @NotNull Long2ObjectMap<ChunkRebuildStatus> chunks) {
		this.rebuildRequestedLastTick += chunks.size();
		chunks.keySet().forEach(this::scheduleChunkRebuild);
	}

	@Override
	protected void remove(@NotNull DynamicLightSource lightSource, @NotNull LongSet chunks) {
		this.rebuildRequestedLastTick += chunks.size();
		chunks.forEach(this::scheduleChunkRebuild);
	}

	@Override
	public void startTick() {
		super.startTick();
		this.rebuildRequestedLastTick = 0;
	}
}
