package net.neoforged.neoforge.client.event;

import net.minecraft.resources.io.ResourceReloader;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

public class RegisterClientReloadListenersEvent extends Event implements IModBusEvent {
	/**
	 * Registers the given reload listener to the client-side resource manager.
	 *
	 * @param reloadListener the reload listener
	 */
	public void registerReloadListener(ResourceReloader reloadListener) {
	}
}
