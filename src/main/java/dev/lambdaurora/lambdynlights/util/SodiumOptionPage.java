/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.util;

import com.google.common.collect.ImmutableList;
import dev.lambdaurora.lambdynlights.LambDynLightsConstants;
import dev.lambdaurora.lambdynlights.gui.SettingsScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

/**
 * Represents utilities to inject a sodium option page for LambDynamicLights.
 *
 * @author LambdAurora
 * @version 4.8.3
 * @since 3.1.0
 */
public final class SodiumOptionPage {
	public static final Component TITLE = Component.translatable(
			"lambdynlights.menu.sodium.tab", SettingsScreen.MOD_NAME
	);
	private static final MethodHandle CREATE_OPTION_PAGE;

	private static final @Nullable SOAPIEntry SOAPI_ENTRY;

	public static Object makeSodiumOptionPage(Component text) {
		try {
			var page = CREATE_OPTION_PAGE.invoke(text, ImmutableList.of());

			if (SOAPI_ENTRY != null) {
				SOAPI_ENTRY.setFor(page);
			}

			return page;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private record SOAPIEntry(Field field, Object id) {
		void setFor(Object optionPage) {
			try {
				field.set(optionPage, id);
			} catch (IllegalAccessException e) {
				// Ignored.
			}
		}
	}

	static {
		try {
			Class<?> optionPage = Class.forName("net.caffeinemc.mods.sodium.client.gui.options.OptionPage");
			CREATE_OPTION_PAGE = MethodHandles.lookup().unreflectConstructor(optionPage.getConstructor(Component.class, ImmutableList.class));

			SOAPIEntry entry = null;
			try {
				Class<?> class$OptionIdentifier = Class.forName("toni.sodiumoptionsapi.api.OptionIdentifier");

				for (var field : optionPage.getDeclaredFields()) {
					if (field.getType().equals(class$OptionIdentifier)) {
						field.setAccessible(true);

						entry = new SOAPIEntry(
								field,
								class$OptionIdentifier.getConstructor(String.class, String.class, Class.class)
										.newInstance(LambDynLightsConstants.NAMESPACE, "settings", void.class)
						);
					}
				}

			} catch (Throwable e) {
				// Ignored
			}
			SOAPI_ENTRY = entry;
		} catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
