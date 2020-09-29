/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Represents a utility class for compatibility.
 *
 * @author LambdAurora
 * @version 1.3.1
 * @since 1.0.0
 */
public final class LambDynLightsCompat
{
    /**
     * Returns whether Canvas is installed.
     *
     * @return True if Canvas is installed, else false.
     */
    public static boolean isCanvasInstalled()
    {
        return FabricLoader.getInstance().isModLoaded("canvas");
    }

    /**
     * Returns whether Lil Tater Reloaded is installed.
     *
     * @return True if LTR is installed, else false.
     */
    public static boolean isLilTaterReloadedInstalled()
    {
        // Don't even think about it Yog.
        return FabricLoader.getInstance().isModLoaded("ltr");
    }

    /**
     * Returns whether Sodium is installed.
     *
     * @return True if Sodium is installed, else false.
     */
    public static boolean isSodiumInstalled()
    {
        return FabricLoader.getInstance().isModLoaded("sodium");
    }

    /**
     * Returns whether Sodium 0.1.0 is installed.
     *
     * @return True if Sodium 0.1.0 is installed, else false.
     */
    public static boolean isSodium010Installed()
    {
        return FabricLoader.getInstance().getModContainer("sodium").map(mod -> mod.getMetadata().getVersion().getFriendlyString().equalsIgnoreCase("0.1.0"))
                .orElse(false);
    }
}
