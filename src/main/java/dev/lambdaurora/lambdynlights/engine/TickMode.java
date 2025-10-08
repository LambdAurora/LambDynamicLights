/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine;

/**
 * Represents a ticking mode.
 *
 * @author Akarys
 * @version 4.8.0
 * @since 4.8.0
 */
public enum TickMode {
	REAL_TIME(1),
	SLOW(5),
	SLOWER(10),
	BACKGROUND(20);

	private final int delay;

	TickMode(int delay) {
		this.delay = delay;
	}

	/**
	 * {@return the delay in ticks}
	 */
	public int delay() {
		return this.delay;
	}

	public TickMode min(TickMode other) {
		if (this.delay < other.delay)
			return other;
		return this;
	}

	public TickMode max(TickMode other) {
		if (this.delay > other.delay)
			return other;
		return this;
	}
}
