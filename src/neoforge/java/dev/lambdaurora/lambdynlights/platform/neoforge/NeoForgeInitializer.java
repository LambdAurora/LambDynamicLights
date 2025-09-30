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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = LambDynLightsConstants.NAMESPACE + "_runtime", dist = Dist.CLIENT)
public class NeoForgeInitializer {
	public NeoForgeInitializer(ModContainer nativeContainer, IEventBus modBus) {
		nativeContainer.registerExtensionPoint(IConfigScreenFactory.class, NeoForgeConfigScreenProvider.INSTANCE);

		ModList.get().getModContainerById(LambDynLightsConstants.NAMESPACE)
				.ifPresent(runtimeContainer -> {
					runtimeContainer.registerExtensionPoint(
							IConfigScreenFactory.class,
							NeoForgeConfigScreenProvider.INSTANCE
					);
				});

		modBus.addListener(AddClientReloadListenersEvent.class, event -> {
			for (var entry : NeoForgePlatform.INSTANCE.reloaders) {
				event.addListener(entry.id(), entry.reloader());

				for (var dependency : entry.dependencies()) {
					event.addDependency(dependency, entry.id());
				}
			}
		});
	}
}
