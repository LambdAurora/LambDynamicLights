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
import dev.lambdaurora.lambdynlights.engine.lookup.SpatialLookupCollectionEntry;
import dev.lambdaurora.lambdynlights.engine.lookup.SpatialLookupEntry;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public class CollectionDynamicLightSource implements DynamicLightSource {
	private boolean dirty;
	private Collection<Entry> entries;

	public CollectionDynamicLightSource(Collection<Entry> entries) {
		this.entries = entries;
		this.dirty = true;
	}

	@Override
	public Stream<SpatialLookupEntry> splitIntoDynamicLightEntries(@NotNull CellHasher cellHasher) {
		record Data(LongList position, ByteList luminance) {}
		var cellKeyToData = new Int2ObjectOpenHashMap<Data>();

		for (var entry : this.entries) {
			int cellKey = cellHasher.hashAt(entry.x(), entry.y(), entry.z());

			var data = cellKeyToData.computeIfAbsent(cellKey, k -> new Data(new LongArrayList(), new ByteArrayList()));

			data.position.add(BlockPos.asLong(entry.x(), entry.y(), entry.z()));
			data.luminance.add((byte) entry.luminance());
		}

		return cellKeyToData.int2ObjectEntrySet()
				.stream()
				.map(entry -> new SpatialLookupCollectionEntry(
						entry.getIntKey(),
						entry.getValue().position.toLongArray(),
						entry.getValue().luminance.toByteArray()
				));
	}

	@Override
	public LongSet getDynamicLightChunksToRebuild(boolean forced) {
		if (!forced && !this.dirty) {
			return LongSet.of();
		}

		var chunks = new LongOpenHashSet();

		for (var entry : this.entries) {
			DynamicLightSource.gatherClosestChunks(chunks, entry.x, entry.y, entry.z);
		}

		return chunks;
	}

	public static CollectionDynamicLightSource cuboid(int startX, int startY, int startZ, int endX, int endY, int endZ, int luminance) {
		var entries = new ArrayList<Entry>();

		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				for (int z = startZ; z <= endZ; z++) {
					entries.add(new Entry(x, y, z, luminance));
				}
			}
		}

		return new CollectionDynamicLightSource(entries);
	}

	public record Entry(int x, int y, int z, int luminance) {
	}
}
