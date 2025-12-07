package net.neoforged.neoforge.event;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.Event;

public abstract class SortedReloadListenerEvent extends Event {
	/**
	 * Adds a new {@link PreparableReloadListener reload listener} to the resource manager.
	 * <p>
	 * Unless explicitly specified, this listener will run after all vanilla listeners, in the order it was registered.
	 *
	 * @param key      The resource location that identifies the reload listener for dependency sorting.
	 * @param listener The listener to add.
	 *
	 * @throws IllegalArgumentException if another listener with that key was already registered.
	 */
	public void addListener(Identifier key, PreparableReloadListener listener) {
	}

	/**
	 * Adds a new dependency entry, such that {@code first} must run before {@code second}.
	 * <p>
	 * Introduction of dependency cycles (first->second->first) will cause an error when the event is finished.
	 *
	 * @param first  The key of the reload listener that must run first.
	 * @param second The key of the reload listener that must run after {@code first}.
	 *
	 * @throws IllegalArgumentException if either {@code first} or {@code second} has not been registered via {@link #addListener}.
	 */
	public void addDependency(Identifier first, Identifier second) {
	}
}
