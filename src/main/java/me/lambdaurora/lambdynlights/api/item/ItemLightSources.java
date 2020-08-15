/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.api.item;

import com.google.gson.JsonObject;
import me.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.aperlambda.lambdacommon.LambdaConstants;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents an item light sources manager.
 *
 * @author LambdAurora
 * @version 1.3.0
 * @since 1.3.0
 */
public final class ItemLightSources
{
    private static final List<ItemLightSource> ITEM_LIGHT_SOURCES = new ArrayList<>();

    /**
     * Loads the item light source data from resource pack.
     *
     * @param resourceManager The resource manager.
     */
    public static void load(@NotNull ResourceManager resourceManager)
    {
        ITEM_LIGHT_SOURCES.clear();

        resourceManager.findResources("dynamiclights/item", path -> path.endsWith(".json")).forEach(id -> load(resourceManager, id));
    }

    private static void load(@NotNull ResourceManager resourceManager, @NotNull Identifier resourceId)
    {
        Identifier id = new Identifier(resourceId.getNamespace(), resourceId.getPath().replace(".json", ""));
        try {
            InputStream stream = resourceManager.getResource(resourceId).getInputStream();
            JsonObject json = LambdaConstants.JSON_PARSER.parse(new InputStreamReader(stream)).getAsJsonObject();

            Optional<ItemLightSource> result = ItemLightSource.fromJson(id, json);
            if (!result.isPresent()) {
                return;
            }

            register(result.get());
        } catch (IOException | IllegalStateException e) {
            LambDynLights.get().warn("Failed to load item light source \"" + id + "\".");
        }
    }

    /**
     * Registers an item light source data.
     *
     * @param data The item light source data.
     */
    public static void register(@NotNull ItemLightSource data)
    {
        for (ItemLightSource other : ITEM_LIGHT_SOURCES) {
            if (other.item == data.item) {
                LambDynLights.get().warn("Failed to register item light source \"" + data.id + "\", duplicates item \""
                        + Registry.ITEM.getId(data.item) + "\" found in \"" + other.id + "\".");
                return;
            }
        }

        ITEM_LIGHT_SOURCES.add(data);
    }

    /**
     * Returns the luminance of the item in the stack.
     *
     * @param stack            The item stack.
     * @param submergedInWater True if the stack is submerged in water, else false.
     * @return A luminance value.
     */
    public static int getLuminance(@NotNull ItemStack stack, boolean submergedInWater)
    {
        for (ItemLightSource data : ITEM_LIGHT_SOURCES) {
            if (data.item == stack.getItem()) {
                return data.getLuminance(submergedInWater);
            }
        }
        if (stack.getItem() instanceof BlockItem)
            return ((BlockItem) stack.getItem()).getBlock().getDefaultState().getLuminance();
        return 0;
    }
}
