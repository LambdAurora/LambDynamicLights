/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import dev.lambdaurora.lambdynlights.engine.scheduler.ChunkRebuildScheduler;
import dev.lambdaurora.lambdynlights.engine.scheduler.CullingChunkRebuildScheduler;
import dev.lambdaurora.lambdynlights.engine.scheduler.SimpleChunkRebuildScheduler;
import dev.lambdaurora.lambdynlights.util.DynamicLightDebugRenderer;
import dev.lambdaurora.spruceui.util.Nameable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents the available chunk rebuild scheduler modes.
 *
 * @author LambdAurora
 * @version 4.8.0
 * @since 4.8.0
 */
public enum ChunkRebuildSchedulerMode implements Nameable {
	IMMEDIATE(
			SimpleChunkRebuildScheduler::new,
			Component.translatable("lambdynlights.option.chunk_rebuild_scheduler.mode.immediate").withStyle(ChatFormatting.RED)
	),
	CULLING(
			CullingChunkRebuildScheduler::new,
			Component.translatable("lambdynlights.option.chunk_rebuild_scheduler.mode.culling").withStyle(ChatFormatting.GREEN)
	);

	private final Function<DynamicLightDebugRenderer.SectionRebuild, ChunkRebuildScheduler> factory;
	private final Component translatedText;

	ChunkRebuildSchedulerMode(
			Function<DynamicLightDebugRenderer.SectionRebuild, ChunkRebuildScheduler> factory,
			Component translatedText
	) {
		this.factory = factory;
		this.translatedText = translatedText;
	}

	public ChunkRebuildScheduler create(DynamicLightDebugRenderer.SectionRebuild sectionRebuildRenderer) {
		return factory.apply(sectionRebuildRenderer);
	}

	/**
	 * Returns the next dynamic lights mode available.
	 *
	 * @return the next available dynamic lights mode
	 */
	public ChunkRebuildSchedulerMode next() {
		ChunkRebuildSchedulerMode[] v = values();
		if (v.length == this.ordinal() + 1)
			return v[0];
		return v[this.ordinal() + 1];
	}

	/**
	 * Returns the translated text of the dynamic lights mode.
	 *
	 * @return the translated text of the dynamic lights mode
	 */
	public Component getTranslatedText() {
		return this.translatedText;
	}

	@Override
	public String getName() {
		return this.name().toLowerCase();
	}

	/**
	 * Gets the dynamic lights mode from its identifier.
	 *
	 * @param id the identifier of the dynamic lights mode
	 * @return the dynamic lights mode if found, else empty
	 */
	public static Optional<ChunkRebuildSchedulerMode> byId(String id) {
		return Arrays.stream(values()).filter(mode -> mode.getName().equalsIgnoreCase(id)).findFirst();
	}
}
