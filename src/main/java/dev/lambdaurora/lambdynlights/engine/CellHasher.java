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
 * Represents a cell hasher.
 *
 * @author LambdAurora
 * @version 4.4.0
 * @since 4.4.0
 */
public interface CellHasher {
	/**
	 * {@return the cell hash at the given block position}
	 *
	 * @param x the X block coordinate
	 * @param y the Y block coordinate
	 * @param z the Z block coordinate
	 */
	int hashAt(int x, int y, int z);

	/**
	 * Hashes the given cell coordinates.
	 *
	 * @param cellX the cell X-coordinate
	 * @param cellY the cell Y-coordinate
	 * @param cellZ the cell Z-coordinate
	 * @return the cell hash
	 */
	int hashCell(int cellX, int cellY, int cellZ);
}
