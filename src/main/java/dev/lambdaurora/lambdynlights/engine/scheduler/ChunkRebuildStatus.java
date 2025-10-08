/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine.scheduler;

import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

/**
 * Represents the status of a chunk section rebuild in the context of dynamic lighting.
 *
 * @author LambdAurora
 * @version 4.8.0
 * @since 4.8.0
 */
public enum ChunkRebuildStatus {
	REQUESTED(0xff9b00a6),
	AFFECTED(0xff00ff00),
	REQUESTED_AGAIN(0xff9b00a6),
	REMOVE_REQUESTED(0xffff0000);

	@Unmodifiable
	public static final List<ChunkRebuildStatus> VALUES = List.of(values());
	private final int color;

	ChunkRebuildStatus(int color) {
		this.color = color;
	}

	@VisibleForTesting
	public int color() {
		return this.color;
	}

	public boolean needsRebuild() {
		return this != AFFECTED;
	}

	public boolean needsCleanup() {
		return this == AFFECTED || this == REQUESTED_AGAIN;
	}
}
