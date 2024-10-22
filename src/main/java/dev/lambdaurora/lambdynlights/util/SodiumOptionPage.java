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
import dev.lambdaurora.lambdynlights.LambDynLightsCompat;
import net.minecraft.network.chat.Text;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Represents utilities to inject a sodium option page for LambDynamicLights.
 *
 * @author LambdAurora
 * @version 3.1.0
 * @since 3.1.0
 */
public final class SodiumOptionPage {
	private static final MethodHandle CREATE_OPTION_PAGE;

	public static Object makeSodiumOptionPage(Text text) {
		try {
			return CREATE_OPTION_PAGE.invoke(text, ImmutableList.of());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	static {
		try {
			Class<?> optionPage = LambDynLightsCompat.isSodium05XInstalled()
					? Class.forName("me.jellysquid.mods.sodium.client.gui.options.OptionPage")
					: Class.forName("net.caffeinemc.mods.sodium.client.gui.options.OptionPage");
			CREATE_OPTION_PAGE = MethodHandles.lookup().unreflectConstructor(optionPage.getConstructor(Text.class, ImmutableList.class));
		} catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
