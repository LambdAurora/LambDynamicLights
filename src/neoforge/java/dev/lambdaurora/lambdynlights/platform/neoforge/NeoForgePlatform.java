/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.platform.neoforge;

import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import dev.lambdaurora.lambdynlights.platform.Platform;
import dev.lambdaurora.lambdynlights.resource.LightSourceLoader;
import dev.yumi.commons.event.ListenableEvent;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.YumiEvents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Provides the NeoForge-specific platform operations.
 *
 * @author LambdAurora
 * @version 4.5.0
 * @since 4.5.0
 */
public final class NeoForgePlatform implements Platform {
	private final ModContainer mod;
	private final IEventBus eventBus;

	public NeoForgePlatform(ModContainer mod) {
		this.mod = mod;

		var nativeContainer = ModList.get().getModContainerById(this.mod.id())
				.orElseThrow(() -> new IllegalStateException(
						"Could not find NeoForge mod container despite mod being initialized as %s."
								.formatted(this.mod.id())
				));
		this.eventBus = nativeContainer.getEventBus();

		nativeContainer.registerExtensionPoint(IConfigScreenFactory.class, NeoForgeConfigScreenProvider.INSTANCE);

		if (!mod.id().equals(LambDynLightsConstants.NAMESPACE)) {
			ModList.get().getModContainerById(LambDynLightsConstants.NAMESPACE)
					.orElseThrow(() -> new IllegalStateException(
							"Could not find the outer %s NeoForge mod container."
									.formatted(LambDynLightsConstants.NAMESPACE)
					))
					.registerExtensionPoint(IConfigScreenFactory.class, NeoForgeConfigScreenProvider.INSTANCE);
		}
	}

	@Override
	public void registerReloader(LightSourceLoader<?> reloader) {
		this.eventBus.addListener(AddClientReloadListenersEvent.class, event -> {
			event.addListener(reloader.id(), reloader);

			for (var dependency : reloader.dependencies()) {
				event.addDependency(dependency, reloader.id());
			}
		});
	}

	@Override
	public ListenableEvent<Identifier, Consumer<RegistryAccess>> getTagLoadedEvent() {
		return new ListenableEvent<>() {
			@Override
			public @NotNull Identifier defaultPhaseId() {
				return YumiEvents.EVENTS.defaultPhaseId();
			}

			@Override
			public void register(@NotNull Identifier phaseIdentifier, @NotNull Consumer<RegistryAccess> listener) {
				NeoForge.EVENT_BUS.addListener(TagsUpdatedEvent.class, event -> {
					if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
						listener.accept((RegistryAccess) event.getLookupProvider());
					}
				});
			}

			@Override
			public void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
			}
		};
	}

	@Override
	public LightSourceLoader.ApplicationPredicate getLightSourceLoaderApplicationPredicate() {
		return (loader, ops, loadedData) -> true;
	}
}
