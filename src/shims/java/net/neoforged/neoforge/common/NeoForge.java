package net.neoforged.neoforge.common;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;

public class NeoForge {
	/**
	 * The NeoForge event bus, used for most events.
	 * Also known as the "game" bus.
	 */
	public static final IEventBus EVENT_BUS = BusBuilder.builder().startShutdown().classChecker(eventType -> {
		if (IModBusEvent.class.isAssignableFrom(eventType)) {
			throw new IllegalArgumentException("IModBusEvent events are not allowed on the common NeoForge bus! Use a mod bus instead.");
		}
	}).build();
}
