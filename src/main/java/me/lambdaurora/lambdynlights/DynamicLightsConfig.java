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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents the mod configuration.
 *
 * @author LambdAurora
 * @version 1.2.1
 * @since 1.0.0
 */
public class DynamicLightsConfig
{
    private static final DynamicLightsMode     DEFAULT_DYNAMIC_LIGHTS_MODE         = DynamicLightsMode.OFF;
    private static final boolean               DEFAULT_ENTITIES_LIGHT_SOURCE       = true;
    private static final boolean               DEFAULT_BLOCK_ENTITIES_LIGHT_SOURCE = true;
    private static final boolean               DEFAULT_WATER_SENSITIVE_CHECK       = true;
    private static final ExplosiveLightingMode DEFAULT_CREEPER_LIGHTING_MODE       = ExplosiveLightingMode.SIMPLE;
    private static final ExplosiveLightingMode DEFAULT_TNT_LIGHTING_MODE           = ExplosiveLightingMode.OFF;

    public static final Path                  CONFIG_FILE_PATH = Paths.get("config/lambdynlights.toml");
    protected final     FileConfig            config;
    private final       LambDynLights         mod;
    private             boolean               firstTime;
    private             DynamicLightsMode     dynamicLightsMode;
    private             ExplosiveLightingMode creeperLightingMode;
    private             ExplosiveLightingMode tntLightingMode;

    public final Option dynamicLightsModeOption = new SpruceCyclingOption("lambdynlights.option.mode",
            amount -> this.setDynamicLightsMode(this.dynamicLightsMode.next()),
            option -> option.getDisplayText(this.dynamicLightsMode.getTranslatedText()),
            new TranslatableText("lambdynlights.tooltip.mode.1")
                    .append(new LiteralText("\n"))
                    .append(new TranslatableText("lambdynlights.tooltip.mode.2", DynamicLightsMode.FASTEST.getTranslatedText(), DynamicLightsMode.FAST.getTranslatedText()))
                    .append(new LiteralText("\n"))
                    .append(new TranslatableText("lambdynlights.tooltip.mode.3", DynamicLightsMode.FANCY.getTranslatedText())));

    public DynamicLightsConfig(@NotNull LambDynLights mod)
    {
        this.mod = mod;

        this.firstTime = Files.notExists(CONFIG_FILE_PATH);

        this.config = FileConfig.builder(CONFIG_FILE_PATH).concurrent().defaultResource("/lambdynlights.toml").autosave().build();
    }

    /**
     * Loads the configuration.
     */
    public void load()
    {
        this.config.load();

        String dynamicLightsModeValue = this.config.getOrElse("mode", DEFAULT_DYNAMIC_LIGHTS_MODE.getName());
        this.dynamicLightsMode = DynamicLightsMode.byId(dynamicLightsModeValue)
                .orElse(DEFAULT_DYNAMIC_LIGHTS_MODE);
        this.creeperLightingMode = ExplosiveLightingMode.byId(this.config.getOrElse("light_sources.creeper", DEFAULT_CREEPER_LIGHTING_MODE.getName()))
                .orElse(DEFAULT_CREEPER_LIGHTING_MODE);
        this.tntLightingMode = ExplosiveLightingMode.byId(this.config.getOrElse("light_sources.tnt", DEFAULT_TNT_LIGHTING_MODE.getName()))
                .orElse(DEFAULT_TNT_LIGHTING_MODE);

        if (dynamicLightsModeValue.equalsIgnoreCase("none")) {
            this.firstTime = true;
        }

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
        this.setEntitiesLightSource(DEFAULT_ENTITIES_LIGHT_SOURCE);
        this.setBlockEntitiesLightSource(DEFAULT_BLOCK_ENTITIES_LIGHT_SOURCE);
        this.setWaterSensitiveCheck(DEFAULT_WATER_SENSITIVE_CHECK);
        this.setCreeperLightingMode(DEFAULT_CREEPER_LIGHTING_MODE);
        this.setTntLightingMode(DEFAULT_TNT_LIGHTING_MODE);
    }

    /**
     * Returns whether it's the first time the mod is loaded.
     *
     * @return True if it's the first time, else false.
     */
    public boolean isFirstTime()
    {
        return this.firstTime;
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

        this.firstTime = false;
    }

    /**
     * Returns whether block entities as light source is enabled.
     *
     * @return True if block entities as light source is enabled, else false.
     */
    public boolean hasEntitiesLightSource()
    {
        return this.config.getOrElse("light_sources.entities", DEFAULT_ENTITIES_LIGHT_SOURCE);
    }

    /**
     * Sets whether block entities as light source is enabled.
     *
     * @param enabled True if block entities as light source is enabled, else false.
     */
    public void setEntitiesLightSource(boolean enabled)
    {
        if (!enabled)
            this.mod.removeEntitiesLightSource();
        this.config.set("light_sources.entities", enabled);
    }

    /**
     * Returns whether block entities as light source is enabled.
     *
     * @return True if block entities as light source is enabled, else false.
     */
    public boolean hasBlockEntitiesLightSource()
    {
        return this.config.getOrElse("light_sources.block_entities", DEFAULT_BLOCK_ENTITIES_LIGHT_SOURCE);
    }

    /**
     * Sets whether block entities as light source is enabled.
     *
     * @param enabled True if block entities as light source is enabled, else false.
     */
    public void setBlockEntitiesLightSource(boolean enabled)
    {
        if (!enabled)
            this.mod.removeBlockEntitiesLightSource();
        this.config.set("light_sources.block_entities", enabled);
    }

    /**
     * Returns whether water sensitive check is enabled or not.
     *
     * @return True if water sensitive check is enabled, else false.
     */
    public boolean hasWaterSensitiveCheck()
    {
        return this.config.getOrElse("light_sources.water_sensitive_check", DEFAULT_WATER_SENSITIVE_CHECK);
    }

    /**
     * Sets whether water sensitive check is enabled or not.
     *
     * @param waterSensitive True if water sensitive check is enabled, else false.
     */
    public void setWaterSensitiveCheck(boolean waterSensitive)
    {
        this.config.set("light_sources.water_sensitive_check", waterSensitive);
    }

    /**
     * Returns the Creeper dynamic lighting mode.
     *
     * @return The Creeper dynamic lighting mode.
     */
    public ExplosiveLightingMode getCreeperLightingMode()
    {
        return this.creeperLightingMode;
    }

    /**
     * Sets the Creeper dynamic lighting mode.
     *
     * @param lightingMode The Creeper dynamic lighting mode.
     */
    public void setCreeperLightingMode(@NotNull ExplosiveLightingMode lightingMode)
    {
        this.creeperLightingMode = lightingMode;

        if (!lightingMode.isEnabled())
            this.mod.removeCreeperLightSources();
        this.config.set("light_sources.creeper", lightingMode.getName());
    }

    /**
     * Returns the TNT dynamic lighting mode.
     *
     * @return The TNT dynamic lighting mode.
     */
    public ExplosiveLightingMode getTntLightingMode()
    {
        return this.tntLightingMode;
    }

    /**
     * Sets the TNT dynamic lighting mode.
     *
     * @param lightingMode The TNT dynamic lighting mode.
     */
    public void setTntLightingMode(@NotNull ExplosiveLightingMode lightingMode)
    {
        this.tntLightingMode = lightingMode;

        if (!lightingMode.isEnabled())
            this.mod.removeTntLightSources();
        this.config.set("light_sources.tnt", lightingMode.getName());
    }
}
