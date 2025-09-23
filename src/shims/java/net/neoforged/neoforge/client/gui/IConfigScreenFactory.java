package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModContainer;

import java.util.function.Supplier;

/**
 * Register an instance to {@link ModContainer#registerExtensionPoint(Class, Supplier)}
 * to supply a config screen for your mod.
 *
 * <p>The config screen will be accessible from the mod list menu.
 */
public interface IConfigScreenFactory extends IExtensionPoint {
	/**
	 * Creates a new config screen. The {@code modListScreen} parameter can be used for a "back" button.
	 */
	Screen createScreen(ModContainer container, Screen modListScreen);
}
