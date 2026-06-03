/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.platform.neoforge;

import dev.lambdaurora.lambdynlights.platform.Platform;
import dev.lambdaurora.lambdynlights.resource.LightSourceLoader;
import dev.yumi.commons.event.ListenableEvent;
import dev.yumi.mc.core.api.YumiEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides the NeoForge-specific platform operations.
 *
 * @author LambdAurora
 * @version 4.10.3
 * @since 4.5.0
 */
public final class NeoForgePlatform implements Platform {
	public static final NeoForgePlatform INSTANCE = new NeoForgePlatform();
	final List<PendingResourceReloader> reloaders = new ArrayList<>();

	private NeoForgePlatform() {}

	@Override
	public void registerReloader(LightSourceLoader<?> reloader) {
		this.reloaders.add(new PendingResourceReloader(reloader.id(), reloader, reloader.dependencies()));
	}

	@Override
	public ListenableEvent<Identifier, Consumer<HolderLookup.Provider>> getTagLoadedEvent() {
		return new ListenableEvent<>() {
			@Override
			public Identifier defaultPhaseId() {
				return YumiEvents.EVENTS.defaultPhaseId();
			}

			@Override
			public void register(Identifier phaseIdentifier, Consumer<HolderLookup.Provider> listener) {
				NeoForge.EVENT_BUS.addListener(TagsUpdatedEvent.ClientPacketReceived.class, event ->
						listener.accept(event.getRegistries())
				);
			}

			@Override
			public void addPhaseOrdering(Identifier firstPhase, Identifier secondPhase) {
			}
		};
	}

	@Override
	public LightSourceLoader.ApplicationPredicate getLightSourceLoaderApplicationPredicate() {
		return (loader, ops, loadedData) -> true;
	}
}
