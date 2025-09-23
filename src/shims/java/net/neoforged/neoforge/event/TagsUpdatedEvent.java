package net.neoforged.neoforge.event;

import net.minecraft.core.RegistryAccess;
import net.neoforged.bus.api.Event;

public class TagsUpdatedEvent extends Event {
	/**
	 * @return The dynamic registries that have had their tags rebound.
	 */
	public RegistryAccess getRegistryAccess() {
		return null;
	}

	public UpdateCause getUpdateCause() {
		return UpdateCause.CLIENT_PACKET_RECEIVED;
	}

	public enum UpdateCause {
		SERVER_DATA_LOAD, CLIENT_PACKET_RECEIVED;
	}
}
