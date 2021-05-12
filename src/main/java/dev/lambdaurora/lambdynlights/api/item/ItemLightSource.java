/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an item light source.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 1.3.0
 */
public record ItemLightSource(Identifier id, Item item, int luminance, boolean waterSensitive) {
    public ItemLightSource(@NotNull Identifier id, @NotNull Item item, int luminance) {
        this(id, item, luminance, false);
    }

    /**
     * Gets the luminance of the item.
     *
     * @param stack The item stack.
     * @param submergedInWater True if submerged in water, else false.
     * @return The luminance value between 0 and 15.
     */
    public int getLuminance(@NotNull ItemStack stack, boolean submergedInWater) {
        if (this.waterSensitive && LambDynLights.get().config.hasWaterSensitiveCheck() && submergedInWater)
            return 0; // Don't emit light with water sensitive items while submerged in water.

        return this.luminance;
    }

    @Override
    public String toString() {
        return "ItemLightSource{" +
                "item=" + item +
                ", luminance=" + luminance +
                ", water_sensitive=" + waterSensitive +
                '}';
    }

    public static @NotNull Optional<ItemLightSource> fromJson(@NotNull Identifier id, @NotNull JsonObject json) {
        if (!json.has("item") || !json.has("luminance")) {
            LambDynLights.get().warn("Failed to parse item light source \"" + id + "\", invalid format: missing required fields.");
            return Optional.empty();
        }

        var affectId = new Identifier(json.get("item").getAsString());
        var item = Registry.ITEM.get(affectId);

        if (item == Items.AIR)
            return Optional.empty();

        int luminance;
        var luminanceElement = json.get("luminance").getAsJsonPrimitive();
        if (luminanceElement.isNumber()) {
            luminance = luminanceElement.getAsInt();
        } else if (luminanceElement.isString()) {
            var luminanceStr = luminanceElement.getAsString();
            if (luminanceStr.equals("block")) {
                if (item instanceof BlockItem blockItem) {
                    luminance = blockItem.getBlock().getDefaultState().getLuminance();
                } else {
                    return Optional.empty();
                }
            } else {
                var block = Registry.BLOCK.get(new Identifier(luminanceStr));
                if (block == Blocks.AIR)
                    return Optional.empty();

                luminance = block.getDefaultState().getLuminance();
            }
        } else {
            LambDynLights.get().warn("Failed to parse item light source \"" + id + "\", invalid format: \"luminance\" field value isn't string or integer.");
            return Optional.empty();
        }

        boolean waterSensitive = false;
        if (json.has("water_sensitive"))
            waterSensitive = json.get("water_sensitive").getAsBoolean();

        return Optional.of(new ItemLightSource(id, item, luminance, waterSensitive));
    }
}
