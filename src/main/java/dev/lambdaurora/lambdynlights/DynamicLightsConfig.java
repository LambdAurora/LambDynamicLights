/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dev.lambdaurora.lambdynlights.config.BooleanSettingEntry;
import dev.lambdaurora.lambdynlights.config.SettingEntry;
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents the mod configuration.
 *
 * @author LambdAurora
 * @version 4.0.0
 * @since 1.0.0
 */
public class DynamicLightsConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambDynamicLights|Config");
	private static final DynamicLightsMode DEFAULT_DYNAMIC_LIGHTS_MODE = DynamicLightsMode.FANCY;
	private static final boolean DEFAULT_ENTITIES_LIGHT_SOURCE = true;
	private static final boolean DEFAULT_SELF_LIGHT_SOURCE = true;
	private static final boolean DEFAULT_WATER_SENSITIVE_CHECK = true;
	private static final ExplosiveLightingMode DEFAULT_CREEPER_LIGHTING_MODE = ExplosiveLightingMode.SIMPLE;
	private static final ExplosiveLightingMode DEFAULT_TNT_LIGHTING_MODE = ExplosiveLightingMode.OFF;
	private static final int DEFAULT_DEBUG_CELL_DISPLAY_RADIUS = 0;
	private static final int DEFAULT_DEBUG_LIGHT_LEVEL_RADIUS = 0;

	public static final Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("lambdynlights.toml");
	protected final FileConfig config;
	private final LambDynLights mod;
	private DynamicLightsMode dynamicLightsMode;
	private final List<SettingEntry<?>> settingEntries;
	private final BooleanSettingEntry entitiesLightSource;
	private final BooleanSettingEntry selfLightSource;
	private final BooleanSettingEntry waterSensitiveCheck;
	private final BooleanSettingEntry beamLighting;
	private final BooleanSettingEntry guardianLaser;
	private final BooleanSettingEntry debugActiveDynamicLightingCells;
	private final BooleanSettingEntry debugDisplayDynamicLightingChunkRebuild;
	private final BooleanSettingEntry debugDisplayHandlerBoundingBox;
	private ExplosiveLightingMode creeperLightingMode;
	private ExplosiveLightingMode tntLightingMode;
	private int debugCellDisplayRadius;
	private int debugLightLevelRadius;

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

		this.config = FileConfig.builder(CONFIG_FILE_PATH)
				.defaultResource("/lambdynlights.toml")
				.autosave()
				.writingMode(WritingMode.REPLACE_ATOMIC)
				.build();
		this.entitiesLightSource = new BooleanSettingEntry("light_sources.entities", DEFAULT_ENTITIES_LIGHT_SOURCE, this.config,
				Text.translatable("lambdynlights.tooltip.entities"));
		this.selfLightSource = new BooleanSettingEntry("light_sources.self", DEFAULT_SELF_LIGHT_SOURCE, this.config,
				Text.translatable("lambdynlights.tooltip.self_light_source"))
				.withOnSet(value -> {
					if (!value) this.mod.removeLightSources(source ->
							source instanceof LocalPlayer player && player == Minecraft.getInstance().player
					);
				});
		this.waterSensitiveCheck = new BooleanSettingEntry("light_sources.water_sensitive_check", DEFAULT_WATER_SENSITIVE_CHECK, this.config,
				Text.translatable("lambdynlights.tooltip.water_sensitive"));
		this.beamLighting = new BooleanSettingEntry(
				"light_sources.beam", true, this.config,
				Text.translatable("lambdynlights.option.light_sources.beam.tooltip")
		);
		this.guardianLaser = new BooleanSettingEntry(
				"light_sources.guardian_laser", true, this.config,
				Text.translatable("lambdynlights.option.light_sources.guardian_laser.tooltip")
		);
		this.debugActiveDynamicLightingCells = new BooleanSettingEntry(
				"debug.active_dynamic_lighting_cells", false, this.config,
				Text.translatable("lambdynlights.option.debug.active_dynamic_lighting_cells.tooltip")
		);
		this.debugDisplayDynamicLightingChunkRebuild = new BooleanSettingEntry(
				"debug.display_dynamic_lighting_chunk_rebuild", false, this.config,
				Text.translatable("lambdynlights.option.debug.display_dynamic_lighting_chunk_rebuild.tooltip")
		);
		this.debugDisplayHandlerBoundingBox = new BooleanSettingEntry(
				"debug.display_behavior_bounding_box", false, this.config,
				Text.translatable("lambdynlights.option.debug.display_behavior_bounding_box.tooltip")
		);

		this.settingEntries = List.of(
				this.entitiesLightSource,
				this.selfLightSource,
				this.waterSensitiveCheck,
				this.beamLighting,
				this.guardianLaser,
				this.debugActiveDynamicLightingCells,
				this.debugDisplayDynamicLightingChunkRebuild,
				this.debugDisplayHandlerBoundingBox
		);
	}

	/**
	 * Loads the configuration.
	 */
	public void load() {
		this.config.load();

		String dynamicLightsModeValue = this.config.getOrElse("mode", DEFAULT_DYNAMIC_LIGHTS_MODE.getName());
		this.dynamicLightsMode = DynamicLightsMode.byId(dynamicLightsModeValue)
				.orElse(DEFAULT_DYNAMIC_LIGHTS_MODE);
		this.settingEntries.forEach(entry -> entry.load(this.config));
		this.creeperLightingMode = ExplosiveLightingMode.byId(this.config.getOrElse("light_sources.creeper", DEFAULT_CREEPER_LIGHTING_MODE.getName()))
				.orElse(DEFAULT_CREEPER_LIGHTING_MODE);
		this.tntLightingMode = ExplosiveLightingMode.byId(this.config.getOrElse("light_sources.tnt", DEFAULT_TNT_LIGHTING_MODE.getName()))
				.orElse(DEFAULT_TNT_LIGHTING_MODE);
		this.debugCellDisplayRadius = this.config.getOrElse("debug.cell_display_radius", DEFAULT_DEBUG_CELL_DISPLAY_RADIUS);
		this.debugLightLevelRadius = this.config.getOrElse("debug.light_level_radius", DEFAULT_DEBUG_LIGHT_LEVEL_RADIUS);

		LambDynLights.log(LOGGER, "Configuration loaded.");
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
		this.settingEntries.forEach(SettingEntry::reset);
		this.setCreeperLightingMode(DEFAULT_CREEPER_LIGHTING_MODE);
		this.setTntLightingMode(DEFAULT_TNT_LIGHTING_MODE);
		this.setDebugCellDisplayRadius(DEFAULT_DEBUG_CELL_DISPLAY_RADIUS);
		this.setDebugLightLevelRadius(DEFAULT_DEBUG_LIGHT_LEVEL_RADIUS);
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
	 * Sets the dynamic lighting mode.
	 *
	 * @param mode the dynamic lights mode
	 */
	public void setDynamicLightsMode(@NotNull DynamicLightsMode mode) {
		if (this.dynamicLightsMode.isEnabled() != mode.isEnabled()) {
			this.mod.shouldForceRefresh = true;
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
		this.config.set("light_sources.tnt", lightingMode.getName());
	}

	/**
	 * {@return the beacon/gateway beam light source setting holder}
	 */
	public BooleanSettingEntry getBeamLighting() {
		return this.beamLighting;
	}

	/**
	 * {@return the guardian laser light source setting holder}
	 */
	public BooleanSettingEntry getGuardianLaser() {
		return this.guardianLaser;
	}

	/**
	 * {@return the active dynamic lighting cells debug setting holder}
	 */
	public BooleanSettingEntry getDebugActiveDynamicLightingCells() {
		return this.debugActiveDynamicLightingCells;
	}

	public int getDebugCellDisplayRadius() {
		return this.debugCellDisplayRadius;
	}

	public void setDebugCellDisplayRadius(int debugCellDisplayRadius) {
		this.debugCellDisplayRadius = debugCellDisplayRadius;
		this.config.set("debug.cell_display_radius", debugCellDisplayRadius);
	}

	/**
	 * {@return the dynamic lighting chunk rebuilds display debug setting holder}
	 */
	public BooleanSettingEntry getDebugDisplayDynamicLightingChunkRebuilds() {
		return this.debugDisplayDynamicLightingChunkRebuild;
	}

	public BooleanSettingEntry getDebugDisplayHandlerBoundingBox() {
		return this.debugDisplayHandlerBoundingBox;
	}

	public int getDebugLightLevelRadius() {
		return this.debugLightLevelRadius;
	}

	public void setDebugLightLevelRadius(int debugLightLevelRadius) {
		this.debugLightLevelRadius = debugLightLevelRadius;
		this.config.set("debug.light_level_radius", debugCellDisplayRadius);
	}
}
