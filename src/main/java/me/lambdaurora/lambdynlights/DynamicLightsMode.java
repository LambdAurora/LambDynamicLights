/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.aperlambda.lambdacommon.utils.Nameable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents the dynamic lights mode.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public enum DynamicLightsMode implements Nameable
{
    OFF(0, Formatting.RED),
    FASTEST(500, Formatting.GOLD),
    FAST(250, Formatting.YELLOW),
    FANCY(0, Formatting.GREEN);

    private final int        delay;
    private final Formatting formatting;

    DynamicLightsMode(int delay, @NotNull Formatting formatting)
    {
        this.delay = delay;
        this.formatting = formatting;
    }

    /**
     * Returns whether this mode enables dynamic lights.
     *
     * @return True if the mode enables dynamic lights, else false.
     */
    public boolean isEnabled()
    {
        return this != OFF;
    }

    /**
     * Returns whether this mode has an update delay.
     *
     * @return True if the mode has an update delay, else false.
     */
    public boolean hasDelay()
    {
        return this.delay != 0;
    }

    /**
     * Returns the update delay of this mode.
     *
     * @return The mode's update delay.
     */
    public int getDelay()
    {
        return this.delay;
    }

    /**
     * Returns the next dynamic lights mode available.
     *
     * @return The next available dynamic lights mode.
     */
    public DynamicLightsMode next()
    {
        DynamicLightsMode[] v = values();
        if (v.length == this.ordinal() + 1)
            return v[0];
        return v[this.ordinal() + 1];
    }

    public @NotNull String getTranslationKey()
    {
        return this == OFF ? "options.off" : ("lambdynlights.mode." + this.getName());
    }

    /**
     * Returns the translated text of the dynamic lights mode.
     *
     * @return The translated text of the dynamic lights mode.
     */
    public @NotNull Text getTranslatedText()
    {
        return new TranslatableText(this.getTranslationKey()).formatted(this.formatting);
    }

    @Override
    public @NotNull String getName()
    {
        return this.name().toLowerCase();
    }

    /**
     * Gets the dynamic lights mode from its identifier.
     *
     * @param id The identifier of the dynamic lights mode.
     * @return The dynamic lights mode if found, else empty.
     */
    public static @NotNull Optional<DynamicLightsMode> byId(@NotNull String id)
    {
        return Arrays.stream(values()).filter(mode -> mode.getName().equalsIgnoreCase(id)).findFirst();
    }
}
