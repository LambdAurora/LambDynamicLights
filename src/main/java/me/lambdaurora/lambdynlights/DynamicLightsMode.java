/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights;

import me.lambdaurora.spruceui.SpruceTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.aperlambda.lambdacommon.utils.Nameable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents the dynamic lights mode.
 *
 * @author LambdAurora
 * @version 1.2.1
 * @since 1.0.0
 */
public enum DynamicLightsMode implements Nameable
{
    OFF(0, Formatting.RED, SpruceTexts.OPTIONS_OFF),
    FASTEST(500, Formatting.GOLD, SpruceTexts.OPTIONS_GENERIC_FASTEST),
    FAST(250, Formatting.YELLOW, SpruceTexts.OPTIONS_GENERIC_FAST),
    FANCY(0, Formatting.GREEN, SpruceTexts.OPTIONS_GENERIC_FANCY);

    private final int  delay;
    private final Text translatedText;

    DynamicLightsMode(int delay, @NotNull Formatting formatting, @NotNull Text translatedText)
    {
        this.delay = delay;
        this.translatedText = translatedText.copy().formatted(formatting);
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

    /**
     * Returns the translated text of the dynamic lights mode.
     *
     * @return The translated text of the dynamic lights mode.
     */
    public @NotNull Text getTranslatedText()
    {
        return this.translatedText;
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
