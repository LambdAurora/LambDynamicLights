/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import dev.lambdaurora.lambdynlights.config.BooleanSettingEntry;
import dev.lambdaurora.lambdynlights.config.SettingEntry;
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.tooltip.TooltipData;
import dev.yumi.mc.core.api.YumiMods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Represents the mod configuration.
 *
 * @author LambdAurora
 * @version 4.4.0
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

	private static final Executor SAVE_EXECUTOR = Executors.newSingleThreadExecutor();

	public static final Path CONFIG_FILE_PATH = YumiMods.get().getConfigDirectory()
			.resolve("lambdynlights.toml")
			.normalize();
	private final CommentedConfig config;
	private final LambDynLights mod;
	private DynamicLightsMode dynamicLightsMode;
	private final List<SettingEntry<?>> settingEntries;
	private final BooleanSettingEntry entitiesLightSource;
	private final BooleanSettingEntry selfLightSource;
	private final BooleanSettingEntry waterSensitiveCheck;
	private final BooleanSettingEntry beamLighting;
	private final BooleanSettingEntry fireflyLighting;
	private final BooleanSettingEntry guardianLaser;
	private final BooleanSettingEntry sonicBoomLighting;
	private final BooleanSettingEntry glowingEffectLighting;
	private final BooleanSettingEntry debugActiveDynamicLightingCells;
	private final BooleanSettingEntry debugDisplayDynamicLightingChunkRebuild;
	private final BooleanSettingEntry debugDisplayHandlerBoundingBox;
	private ExplosiveLightingMode creeperLightingMode;
	private ExplosiveLightingMode tntLightingMode;
	private int debugCellDisplayRadius;
	private int debugLightLevelRadius;

	private int lastHash;

	public final SpruceOption dynamicLightsModeOption = new SpruceCyclingOption("lambdynlights.option.mode",
			amount -> this.setDynamicLightsMode(this.dynamicLightsMode.next()),
			option -> option.getDisplayText(this.dynamicLightsMode.getTranslatedText()),
			TooltipData.builder().text(Text.translatable("lambdynlights.tooltip.mode.1")
					.append(Text.literal("\n"))
					.append(Text.translatable("lambdynlights.tooltip.mode.2", DynamicLightsMode.FASTEST.getTranslatedText(), DynamicLightsMode.FAST.getTranslatedText()))
					.append(Text.literal("\n"))
					.append(Text.translatable("lambdynlights.tooltip.mode.3", DynamicLightsMode.FANCY.getTranslatedText()))).build());

	public DynamicLightsConfig(@NotNull LambDynLights mod) {
		this.mod = mod;
		this.config = CommentedConfig.inMemory();

		this.entitiesLightSource = new BooleanSettingEntry("light_sources.entities", DEFAULT_ENTITIES_LIGHT_SOURCE, this.config,
				TooltipData.builder().text(Text.translatable("lambdynlights.tooltip.entities")).build());
		this.selfLightSource = new BooleanSettingEntry("light_sources.self", DEFAULT_SELF_LIGHT_SOURCE, this.config,
				TooltipData.builder().text(Text.translatable("lambdynlights.tooltip.self_light_source")).build())
				.withOnSet(value -> {
					if (!value) this.mod.removeLightSources(source ->
							source instanceof LocalPlayer player && player == Minecraft.getInstance().player
					);
				});
		this.waterSensitiveCheck = new BooleanSettingEntry("light_sources.water_sensitive_check", DEFAULT_WATER_SENSITIVE_CHECK, this.config,
				TooltipData.builder().text(Text.translatable("lambdynlights.tooltip.water_sensitive")).build()
		);
		this.beamLighting = new BooleanSettingEntry(
				"light_sources.beam", true, this.config,
				TooltipData.builder().text(Text.translatable("lambdynlights.option.light_sources.beam.tooltip")).build()
		);
		this.fireflyLighting = new BooleanSettingEntry(
				"light_sources.firefly", true, this.config,
				TooltipData.builder().text(Text.translatable("lambdynlights.option.light_sources.firefly.tooltip")).build()
		);
		this.guardianLaser = new BooleanSettingEntry(
				"light_sources.guardian_laser", true, this.config,
				TooltipData.builder().text(Text.translatable("lambdynlights.option.light_sources.guardian_laser.tooltip")).build()
		);
		this.sonicBoomLighting = new BooleanSettingEntry(
				"light_sources.sonic_boom", true, this.config,
				TooltipData.builder().text(Text.translatable("lambdynlights.option.light_sources.sonic_boom.tooltip")).build()
		);
		this.glowingEffectLighting = new BooleanSettingEntry(
				"light_sources.glowing_effect", true, this.config,
				TooltipData.builder().text(Text.translatable("lambdynlights.option.light_sources.glowing_effect.tooltip")).build()
		);
		this.debugActiveDynamicLightingCells = new BooleanSettingEntry(
				"debug.active_dynamic_lighting_cells", false, this.config,
				TooltipData.builder().text(Text.translatable("lambdynlights.option.debug.active_dynamic_lighting_cells.tooltip")).build()
		);
		this.debugDisplayDynamicLightingChunkRebuild = new BooleanSettingEntry(
				"debug.display_dynamic_lighting_chunk_rebuild", false, this.config,
				TooltipData.builder().text(Text.translatable("lambdynlights.option.debug.display_dynamic_lighting_chunk_rebuild.tooltip")).build()
		);
		this.debugDisplayHandlerBoundingBox = new BooleanSettingEntry(
				"debug.display_behavior_bounding_box", false, this.config,
				TooltipData.builder().text(Text.translatable("lambdynlights.option.debug.display_behavior_bounding_box.tooltip")).build()
		);

		this.settingEntries = List.of(
				this.entitiesLightSource,
				this.selfLightSource,
				this.waterSensitiveCheck,
				this.beamLighting,
				this.fireflyLighting,
				this.guardianLaser,
				this.sonicBoomLighting,
				this.glowingEffectLighting,
				this.debugActiveDynamicLightingCells,
				this.debugDisplayDynamicLightingChunkRebuild,
				this.debugDisplayHandlerBoundingBox
		);
	}

	/**
	 * Loads the configuration.
	 */
	public void load() {
		try {
			this.loadFromFile(true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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

		this.lastHash = this.serialize().hashCode();

		LambDynLights.log(LOGGER, "Configuration loaded.");
	}

	private void loadFromFile(boolean firstAttempt) throws IOException {
		try (var reader = Files.newBufferedReader(CONFIG_FILE_PATH)) {
			new TomlParser().parse(reader, this.config, ParsingMode.REPLACE);
		} catch (NoSuchFileException | FileNotFoundException e) {
			if (!firstAttempt) {
				throw e;
			}

			this.copyDefaultFile();
			this.loadFromFile(false);
		} catch (ParsingException e) {
			if (!firstAttempt) {
				throw e;
			}

			var backupPath = CONFIG_FILE_PATH.resolveSibling("lambdynlights.toml.old").toAbsolutePath().normalize();

			LambDynLights.error(LOGGER, "Failed to parse configuration file, THIS IS BAD.", e);
			LambDynLights.error(LOGGER, "Copying the corrupt file to \"{}\".", backupPath);
			Files.copy(CONFIG_FILE_PATH, backupPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			this.copyDefaultFile();
			this.loadFromFile(false);
		}
	}

	/**
	 * Copies the default configuration file from this mod's JAR.
	 *
	 * @throws IOException if copying the file fails
	 */
	private void copyDefaultFile() throws IOException {
		Files.createDirectories(CONFIG_FILE_PATH.getParent());

		var path = LambDynLightsConstants.MOD_CONTAINER
				.findPath("lambdynlights.toml")
				.orElseThrow(() -> new IllegalStateException("This distribution of LambDynamicLights is broken: "
						+ "cannot find the default configuration file inside of the mod's JAR."));

		Files.copy(
				path,
				CONFIG_FILE_PATH,
				StandardCopyOption.REPLACE_EXISTING
		);
		LambDynLights.log(LOGGER, "Copied default configuration file.");
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
	 * Queues the saving of the configuration.
	 */
	public void save() {
		this.maybeSerialize().ifPresent(data -> SAVE_EXECUTOR.execute(() -> this.doSave(data)));
	}

	private @NotNull String serialize() {
		return new TomlWriter().writeToString(this.config);
	}

	private @NotNull Optional<String> maybeSerialize() {
		var data = this.serialize();
		int hash = data.hashCode();

		if (this.lastHash != hash) {
			this.lastHash = hash;
			return Optional.of(data);
		} else {
			return Optional.empty();
		}
	}

	private void doSave(String data) {
		try {
			Files.createDirectories(CONFIG_FILE_PATH.getParent());

			var tmpPath = CONFIG_FILE_PATH.resolveSibling(CONFIG_FILE_PATH.getFileName().toString() + ".tmp");
			Files.writeString(tmpPath, data);
			Files.move(tmpPath, CONFIG_FILE_PATH, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LambDynLights.error(LOGGER, "Failed to save configuration file.", e);
			return;
		}

		LambDynLights.log(LOGGER, "Configuration saved.");
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
	 * {@return the firefly light source setting holder}
	 */
	public BooleanSettingEntry getFireflyLighting() {
		return this.fireflyLighting;
	}

	/**
	 * {@return the guardian laser light source setting holder}
	 */
	public BooleanSettingEntry getGuardianLaser() {
		return this.guardianLaser;
	}

	/**
	 * {@return the sonic boom light source setting holder}
	 */
	public BooleanSettingEntry getSonicBoomLighting() {
		return this.sonicBoomLighting;
	}

	/**
	 * {@return the glowing effect lighting setting holder}
	 */
	public BooleanSettingEntry getGlowingEffectLighting() {
		return this.glowingEffectLighting;
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
