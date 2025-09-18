/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.engine;

import com.mojang.logging.LogUtils;
import dev.lambdaurora.lambdynlights.DynamicLightsConfig;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.accessor.DynamicLightHandlerHolder;
import dev.lambdaurora.lambdynlights.engine.lookup.SpatialLookupEntry;
import dev.lambdaurora.lambdynlights.engine.source.DynamicLightSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Represents the dynamic lighting engine.
 *
 * @author LambdAurora, Akarys
 * @version 4.4.0
 * @since 3.1.0
 */
public final class DynamicLightingEngine implements CellHasher {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final double MAX_RADIUS = 7.75;
	public static final double MAX_RADIUS_SQUARED = MAX_RADIUS * MAX_RADIUS;
	public static final int CELL_SIZE = MathHelper.ceil(MAX_RADIUS);
	public static final int DEFAULT_LIGHT_SOURCES = 1024;
	private static final Vec3i[] CELL_OFFSETS;

	private SpatialLookupEntry[] spatialLookupEntries;
	private int[] startIndices;
	private final long[] computeSpatialLookupTimes = new long[40];
	private int lastEntryCount = 0;
	private final DynamicLightsConfig config;

	public DynamicLightingEngine(DynamicLightsConfig config) {
		this.config = config;

		this.resize(DEFAULT_LIGHT_SOURCES);
	}

	/**
	 * Returns whether the given entity can light up or not.
	 *
	 * @param entity the entity
	 * @param <T> the type of the entity
	 * @return {@code true} if the entity can light up, or {@code false} otherwise
	 */
	public static <T extends Entity> boolean canLightUp(T entity) {
		if (entity == Minecraft.getInstance().player) {
			if (!LambDynLights.get().config.getSelfLightSource().get())
				return false;
		} else if (!LambDynLights.get().config.getEntitiesLightSource().get()) {
			return false;
		}

		var setting = DynamicLightHandlerHolder.cast(entity.getType()).lambdynlights$getSetting();
		return !(setting == null || !setting.get());
	}

	/**
	 * Returns the dynamic light level at the specified position.
	 *
	 * @param pos the position
	 * @return the dynamic light level at the specified position
	 */
	public double getDynamicLightLevel(@NotNull BlockPos pos) {
		if (!this.config.getDynamicLightsMode().isEnabled()) {
			return 0;
		}

		double result = 0;

		var currentCell = new BlockPos.Mutable(
				positionToCell(pos.getX()),
				positionToCell(pos.getY()),
				positionToCell(pos.getZ())
		);
		var cell = currentCell.immutable();

		for (var cellOffset : CELL_OFFSETS) {
			currentCell.setWithOffset(cell, cellOffset);

			int key = this.hashCell(currentCell.getX(), currentCell.getY(), currentCell.getZ());
			int startIndex = this.startIndices[key];

			for (int i = startIndex; i < this.spatialLookupEntries.length; i++) {
				SpatialLookupEntry entry = this.spatialLookupEntries[i];
				if (entry == null || entry.cellKey() != key) break;

				double light = entry.getDynamicLightLevel(pos);
				if (light > result) {
					result = light;
				}
			}
		}

		return MathHelper.clamp(result, 0, 15);
	}

	/**
	 * {@return the average time it took in nanoseconds to compute spatial lookup across 40 ticks}
	 */
	public float getComputeSpatialLookupTime() {
		if (!this.config.getDynamicLightsMode().isEnabled()) {
			return 0.f; // We do not compute the spatial lookup when the mod is disabled.
		}

		return (float) Arrays.stream(this.computeSpatialLookupTimes)
				.filter(value -> value > 0)
				.average()
				.orElse(0);
	}

	@Override
	public int hashAt(int x, int y, int z) {
		return this.hashCell(
				positionToCell(x),
				positionToCell(y),
				positionToCell(z)
		);
	}

	/**
	 * {@return the cell coordinate of the given block position}
	 *
	 * @param coordinate the block position coordinate
	 */
	public static int positionToCell(int coordinate) {
		// Equivalent to coordinate / CELL_SIZE as long as CELL_SIZE is equals to 8.
		return coordinate >> 3;
	}

	@Override
	public int hashCell(int cellX, int cellY, int cellZ) {
		return Math.abs(((cellX + 31) * 19 + cellY) * 41 + cellZ) * 83 & (this.spatialLookupEntries.length - 1);
	}

	/**
	 * {@return the size of the spatial lookup}
	 */
	public int getSize() {
		return this.spatialLookupEntries.length;
	}

	/**
	 * Resets the size of the spatial lookup back to its default size.
	 */
	public void resetSize() {
		this.resize(DEFAULT_LIGHT_SOURCES);
	}

	private void resize(int newLength) {
		if (this.spatialLookupEntries != null && this.spatialLookupEntries.length == newLength) return;

		this.spatialLookupEntries = new SpatialLookupEntry[newLength];
		this.startIndices = new int[newLength];
	}

	/**
	 * Computes the spatial lookup given the light sources.
	 * <p>
	 * The spatial lookup will allow for a very quick and efficient lookup of relevant light sources at a given position.
	 *
	 * @param lightSources the light sources to compute into a spatial lookup
	 */
	public void computeSpatialLookup(Collection<? extends DynamicLightSource> lightSources) {
		long startTime = System.nanoTime();

		var it = this.computeSpatialEntries(lightSources);

		int i = 0;
		while (it.hasNext()) {
			var entry = it.next();
			this.spatialLookupEntries[i] = entry;
			i++;
		}

		for (i = 0; i < this.spatialLookupEntries.length; i++) {
			if (this.spatialLookupEntries[i] == null) {
				this.lastEntryCount = i;
				break;
			}

			int key = this.spatialLookupEntries[i].cellKey();
			int previousKey = i == 0 ? Integer.MAX_VALUE : this.spatialLookupEntries[i - 1].cellKey();

			if (key != previousKey) {
				this.startIndices[key] = i;
			}
		}

		long endTime = System.nanoTime();
		for (i = 0; i < this.computeSpatialLookupTimes.length - 1; i++) {
			this.computeSpatialLookupTimes[i] = this.computeSpatialLookupTimes[i + 1];
		}
		this.computeSpatialLookupTimes[this.computeSpatialLookupTimes.length - 1] = endTime - startTime;
	}

	private Iterator<SpatialLookupEntry> computeSpatialEntries(
			Collection<? extends DynamicLightSource> lightSources
	) {
		var spatialEntries = lightSources.stream()
				.flatMap(lightSource ->
						lightSource.splitIntoDynamicLightEntries(this)
				)
				.toList();

		if (spatialEntries.size() > this.spatialLookupEntries.length * .95f) {
			int newLength = this.spatialLookupEntries.length * 2;
			LambDynLights.info(LOGGER,
					"Resizing spatial lookup, capacity limit of {} reached. New capacity will be {}.",
					this.spatialLookupEntries.length, newLength
			);

			this.resize(newLength);
			return this.computeSpatialEntries(lightSources);
		}

		Arrays.fill(this.spatialLookupEntries, null);
		Arrays.fill(this.startIndices, Integer.MAX_VALUE);

		return spatialEntries.stream()
				.limit(this.spatialLookupEntries.length)
				.sorted(Comparator.comparingInt(SpatialLookupEntry::cellKey))
				.iterator();
	}

	@VisibleForTesting
	public boolean hasEntriesAt(int cellX, int cellY, int cellZ) {
		int key = this.hashCell(cellX, cellY, cellZ);
		return this.startIndices[key] < this.startIndices.length;
	}

	@VisibleForTesting
	public int getEntryCountAt(int cellX, int cellY, int cellZ) {
		int key = this.hashCell(cellX, cellY, cellZ);
		int startIndex = this.startIndices[key];
		int count = 0;
		for (int i = startIndex; i < this.spatialLookupEntries.length; i++) {
			SpatialLookupEntry entry = this.spatialLookupEntries[i];
			if (entry == null || entry.cellKey() != key) break;

			count++;
		}
		return count;
	}

	@VisibleForTesting
	public int getLastEntryCount() {
		return this.lastEntryCount;
	}

	static {
		CELL_OFFSETS = new Vec3i[27];
		int i = 0;

		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					CELL_OFFSETS[i] = new Vec3i(x, y, z);
					i++;
				}
			}
		}
	}
}
