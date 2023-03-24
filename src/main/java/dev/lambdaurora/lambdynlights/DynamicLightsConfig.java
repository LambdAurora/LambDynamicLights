/*
 * Copyright © 2020-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import com.electronwill.nightconfig.core.file.FileConfig;
import dev.lambdaurora.lambdynlights.config.BooleanSettingEntry;
import dev.lambdaurora.lambdynlights.config.SettingEntry;
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents the mod configuration.
 *
 * @author LambdAurora
 * @version 2.2.1
 * @since 1.0.0
 */
public class DynamicLightsConfig {
	private static final DynamicLightsMode DEFAULT_DYNAMIC_LIGHTS_MODE = DynamicLightsMode.FANCY;
	private static final boolean DEFAULT_ENTITIES_LIGHT_SOURCE = true;
	private static final boolean DEFAULT_SELF_LIGHT_SOURCE = true;
	private static final boolean DEFAULT_BLOCK_ENTITIES_LIGHT_SOURCE = true;
	private static final boolean DEFAULT_WATER_SENSITIVE_CHECK = true;
	private static final ExplosiveLightingMode DEFAULT_CREEPER_LIGHTING_MODE = ExplosiveLightingMode.SIMPLE;
	private static final ExplosiveLightingMode DEFAULT_TNT_LIGHTING_MODE = ExplosiveLightingMode.OFF;

	public static final Path CONFIG_FILE_PATH = Paths.get("config/lambdynlights.toml");
	protected final FileConfig config;
	private final LambDynLights mod;
	private DynamicLightsMode dynamicLightsMode;
	private final BooleanSettingEntry entitiesLightSource;
	private final BooleanSettingEntry selfLightSource;
	private final BooleanSettingEntry blockEntitiesLightSource;
	private final BooleanSettingEntry waterSensitiveCheck;
	private ExplosiveLightingMode creeperLightingMode;
	private ExplosiveLightingMode tntLightingMode;

	public final SpruceOption dynamicLightsModeOption = new SpruceCyclingOption("lambdynlights.option.mode",
			amount -> this.setDynamicLightsMode(this.dynamicLightsMode.next()),
			option -> option.getDisplayText(this.dynamicLightsMode.getTranslatedText()),
			Text.translatable("lambdynlights.tooltip.mode.1")
					.append(Text.literal("\n"))
					.append(Text.translatable("lambdynlights.tooltip.mode.2", DynamicLightsMode.FASTEST.getTranslatedText(), DynamicLightsMode.FAST.getTranslatedText()))
					.append(Text.literal("\n"))
					.append(Text.translatable("lambdynlights.tooltip.mode.3", DynamicLightsMode.FANCY.getTranslatedText())));

	public DynamicLightsConfig(@NotNull LambDynLights mod) {
		this.mod = mod;

		this.config = FileConfig.builder(CONFIG_FILE_PATH).concurrent().defaultResource("/lambdynlights.toml").autosave().build();
		this.entitiesLightSource = new BooleanSettingEntry("light_sources.entities", DEFAULT_ENTITIES_LIGHT_SOURCE, this.config,
				Text.translatable("lambdynlights.tooltip.entities"))
				.withOnSet(value -> {
					if (!value) this.mod.removeEntitiesLightSource();
				});
		this.selfLightSource = new BooleanSettingEntry("light_sources.self", DEFAULT_SELF_LIGHT_SOURCE, this.config,
				Text.translatable("lambdynlights.tooltip.self_light_source"))
				.withOnSet(value -> {
					if (!value) this.mod.removeLightSources(source ->
							source instanceof ClientPlayerEntity && source == MinecraftClient.getInstance().player
					);
				});
		this.blockEntitiesLightSource = new BooleanSettingEntry("light_sources.block_entities", DEFAULT_BLOCK_ENTITIES_LIGHT_SOURCE, this.config,
				Text.translatable("lambdynlights.tooltip.block_entities"))
				.withOnSet(value -> {
					if (!value) this.mod.removeBlockEntitiesLightSource();
				});
		this.waterSensitiveCheck = new BooleanSettingEntry("light_sources.water_sensitive_check", DEFAULT_WATER_SENSITIVE_CHECK, this.config,
				Text.translatable("lambdynlights.tooltip.water_sensitive"));
	}

	/**
	 * Loads the configuration.
	 */
	public void load() {
		this.config.load();

		String dynamicLightsModeValue = this.config.getOrElse("mode", DEFAULT_DYNAMIC_LIGHTS_MODE.getName());
		this.dynamicLightsMode = DynamicLightsMode.byId(dynamicLightsModeValue)
				.orElse(DEFAULT_DYNAMIC_LIGHTS_MODE);
		this.entitiesLightSource.load(this.config);
		this.selfLightSource.load(this.config);
		this.blockEntitiesLightSource.load(this.config);
		this.waterSensitiveCheck.load(this.config);
		this.creeperLightingMode = ExplosiveLightingMode.byId(this.config.getOrElse("light_sources.creeper", DEFAULT_CREEPER_LIGHTING_MODE.getName()))
				.orElse(DEFAULT_CREEPER_LIGHTING_MODE);
		this.tntLightingMode = ExplosiveLightingMode.byId(this.config.getOrElse("light_sources.tnt", DEFAULT_TNT_LIGHTING_MODE.getName()))
				.orElse(DEFAULT_TNT_LIGHTING_MODE);

		this.mod.log("Configuration loaded.");
	}

	/**
	 * Loads the setting.
	 *
	 * @param settingEntry the setting to load
	 */
	public void load(SettingEntry<?> settingEntry) {
		settingEntry.load(this.config);
	}

	/**
	 * Saves the configuration.
	 */
	public void save() {
		this.config.save();
	}

	/**
	 * Resets the configuration.
	 */
	public void reset() {
		this.setDynamicLightsMode(DEFAULT_DYNAMIC_LIGHTS_MODE);
		this.getEntitiesLightSource().set(DEFAULT_ENTITIES_LIGHT_SOURCE);
		this.getSelfLightSource().set(DEFAULT_SELF_LIGHT_SOURCE);
		this.getBlockEntitiesLightSource().set(DEFAULT_BLOCK_ENTITIES_LIGHT_SOURCE);
		this.getWaterSensitiveCheck().set(DEFAULT_WATER_SENSITIVE_CHECK);
		this.setCreeperLightingMode(DEFAULT_CREEPER_LIGHTING_MODE);
		this.setTntLightingMode(DEFAULT_TNT_LIGHTING_MODE);
	}

	/**
	 * Returns the dynamic lights mode.
	 *
	 * @return the dynamic lights mode
	 */
	public DynamicLightsMode getDynamicLightsMode() {
		return this.dynamicLightsMode;
	}

	/**
	 * Sets the dynamic lights mode.
	 *
	 * @param mode the dynamic lights mode
	 */
	public void setDynamicLightsMode(@NotNull DynamicLightsMode mode) {
		if (!mode.isEnabled()) {
			this.mod.clearLightSources();
		}

		this.dynamicLightsMode = mode;
		this.config.set("mode", mode.getName());
	}

	/**
	 * {@return the entities as light source setting holder}
	 */
	public BooleanSettingEntry getEntitiesLightSource() {
		return this.entitiesLightSource;
	}

	/**
	 * {@return the first-person player as light source setting holder}
	 */
	public BooleanSettingEntry getSelfLightSource() {
		return this.selfLightSource;
	}

	/**
	 * {@return the block entities as light source setting holder}
	 */
	public BooleanSettingEntry getBlockEntitiesLightSource() {
		return this.blockEntitiesLightSource;
	}

	/**
	 * {@return the water sensitive check setting holder}
	 */
	public BooleanSettingEntry getWaterSensitiveCheck() {
		return this.waterSensitiveCheck;
	}

	/**
	 * Returns the Creeper dynamic lighting mode.
	 *
	 * @return the Creeper dynamic lighting mode
	 */
	public ExplosiveLightingMode getCreeperLightingMode() {
		return this.creeperLightingMode;
	}

	/**
	 * Sets the Creeper dynamic lighting mode.
	 *
	 * @param lightingMode the Creeper dynamic lighting mode
	 */
	public void setCreeperLightingMode(@NotNull ExplosiveLightingMode lightingMode) {
		this.creeperLightingMode = lightingMode;

		if (!lightingMode.isEnabled())
			this.mod.removeCreeperLightSources();
		this.config.set("light_sources.creeper", lightingMode.getName());
	}

	/**
	 * Returns the TNT dynamic lighting mode.
	 *
	 * @return the TNT dynamic lighting mode
	 */
	public ExplosiveLightingMode getTntLightingMode() {
		return this.tntLightingMode;
	}

	/**
	 * Sets the TNT dynamic lighting mode.
	 *
	 * @param lightingMode the TNT dynamic lighting mode
	 */
	public void setTntLightingMode(@NotNull ExplosiveLightingMode lightingMode) {
		this.tntLightingMode = lightingMode;

		if (!lightingMode.isEnabled())
			this.mod.removeTntLightSources();
		this.config.set("light_sources.tnt", lightingMode.getName());
	}
}
