/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights;

import com.electronwill.nightconfig.core.file.FileConfig;
import me.lambdaurora.spruceui.option.SpruceCyclingOption;
import net.minecraft.client.options.Option;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the mod configuration.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class DynamicLightsConfig
{
    private static final DynamicLightsMode DEFAULT_DYNAMIC_LIGHTS_MODE = DynamicLightsMode.OFF;

    protected final FileConfig        config = FileConfig.builder("config/lambdynlights.toml").concurrent().defaultResource("/lambdynlights.toml").autosave().build();
    private final   LambDynLights     mod;
    private         DynamicLightsMode dynamicLightsMode;

    public final Option dynamicLightsModeOption = new SpruceCyclingOption("lambdynlights.options.mode",
            amount -> this.setDynamicLightsMode(this.dynamicLightsMode.next()),
            option -> option.getDisplayPrefix().append(this.dynamicLightsMode.getTranslatedText()),
            new TranslatableText("lambdynlights.tooltip.mode.1")
                    .append(new LiteralText("\n"))
                    .append(new TranslatableText("lambdynlights.tooltip.mode.2", DynamicLightsMode.FASTEST.getTranslatedText(), DynamicLightsMode.FAST.getTranslatedText()))
                    .append(new LiteralText("\n"))
                    .append(new TranslatableText("lambdynlights.tooltip.mode.3", DynamicLightsMode.FANCY.getTranslatedText())));

    public DynamicLightsConfig(@NotNull LambDynLights mod)
    {
        this.mod = mod;
    }

    /**
     * Loads the configuration.
     */
    public void load()
    {
        this.config.load();

        this.dynamicLightsMode = DynamicLightsMode.byId(this.config.getOrElse("mode", DEFAULT_DYNAMIC_LIGHTS_MODE.getName()))
                .orElse(DEFAULT_DYNAMIC_LIGHTS_MODE);

        this.mod.log("Configuration loaded.");
    }

    /**
     * Saves the configuration.
     */
    public void save()
    {
        this.config.save();
    }

    /**
     * Resets the configuration.
     */
    public void reset()
    {
        this.setDynamicLightsMode(DEFAULT_DYNAMIC_LIGHTS_MODE);
    }

    /**
     * Returns the dynamic lights mode.
     *
     * @return The dynamic lights mode.
     */
    public DynamicLightsMode getDynamicLightsMode()
    {
        return this.dynamicLightsMode;
    }

    /**
     * Sets the dynamic lights mode.
     *
     * @param mode The dynamic lights mode.
     */
    public void setDynamicLightsMode(@NotNull DynamicLightsMode mode)
    {
        this.dynamicLightsMode = mode;
        this.config.set("mode", mode.getName());

        if (!mode.isEnabled()) {
            this.mod.clearLightSources();
        }
    }
}
