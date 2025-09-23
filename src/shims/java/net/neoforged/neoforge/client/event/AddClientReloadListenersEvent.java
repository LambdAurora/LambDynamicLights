package net.neoforged.neoforge.client.event;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.event.SortedReloadListenerEvent;

/**
 * This event allows mods to register client-side reload listeners to the resource manager.
 * This event is fired once during the construction of the {@link Minecraft} instance.
 * <p>
 * This event is only fired on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public class AddClientReloadListenersEvent extends SortedReloadListenerEvent implements IModBusEvent {
}
