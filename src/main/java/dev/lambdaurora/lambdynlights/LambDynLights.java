/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehaviorManager;
import dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import dev.lambdaurora.lambdynlights.compat.CompatLayer;
import dev.lambdaurora.lambdynlights.engine.DynamicLightBehaviorSources;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import dev.lambdaurora.lambdynlights.engine.source.DeferredDynamicLightSource;
import dev.lambdaurora.lambdynlights.engine.source.DynamicLightSource;
import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSource;
import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSourceBehavior;
import dev.lambdaurora.lambdynlights.gui.DevModeGui;
import dev.lambdaurora.lambdynlights.mixin.LevelRendererAccessor;
import dev.lambdaurora.lambdynlights.platform.PlatformProvider;
import dev.lambdaurora.lambdynlights.resource.LightSourceLoader;
import dev.lambdaurora.lambdynlights.resource.entity.EntityLightSources;
import dev.lambdaurora.lambdynlights.resource.item.ItemLightSources;
import dev.lambdaurora.lambdynlights.util.DynamicLightBehaviorDebugRenderer;
import dev.lambdaurora.lambdynlights.util.DynamicLightDebugRenderer;
import dev.lambdaurora.lambdynlights.util.DynamicLightLevelDebugRenderer;
import dev.lambdaurora.lambdynlights.util.DynamicLightSectionDebugRenderer;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.yumi.mc.core.api.CrashReportEvents;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.YumiMods;
import dev.yumi.mc.core.api.entrypoint.EntrypointContainer;
import dev.yumi.mc.core.api.entrypoint.client.ClientModInitializer;
import net.minecraft.TextFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FireflyParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SonicBoomParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.ChunkSectionPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

/**
 * Represents the LambDynamicLights mod.
 *
 * @author LambdAurora
 * @version 4.4.0
 * @since 1.0.0
 */
@ApiStatus.Internal
public class LambDynLights implements ClientModInitializer, DynamicLightsContext {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambDynamicLights");
	public static final LambDynLights INSTANCE = new LambDynLights();

	public static final KeyMapping TOGGLE_FPS_DYNAMIC_LIGHTING = new KeyMapping(
			LambDynLightsConstants.NAMESPACE + ".key.toggle_fps_dynamic_lighting",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			KeyMapping.CATEGORY_MISC
	);

	public final DynamicLightsConfig config = new DynamicLightsConfig(this);
	private final LightSourceLoader.ApplicationPredicate.Pending lightSourceApplicationPredicate
			= new LightSourceLoader.ApplicationPredicate.Pending();
	private final ItemLightSources itemLightSources = new ItemLightSources(lightSourceApplicationPredicate);
	private final EntityLightSources entityLightSources
			= new EntityLightSources(this.itemLightSources, lightSourceApplicationPredicate);
	private final DynamicLightBehaviorSources dynamicLightBehaviorSources = new DynamicLightBehaviorSources(this);
	public final DynamicLightingEngine engine = new DynamicLightingEngine(this.config);
	private final Set<DynamicLightSource> dynamicLightSources = new HashSet<>();
	private final Set<DynamicLightSource> toAdd = new HashSet<>();
	private final List<DynamicLightSource> toClear = new ArrayList<>();
	private final ReentrantReadWriteLock lightSourcesLock = new ReentrantReadWriteLock();

	private final DynamicLightDebugRenderer.SectionRebuild sectionRebuildDebugRenderer
			= new DynamicLightDebugRenderer.SectionRebuild(this);
	public final @Unmodifiable List<DynamicLightDebugRenderer> debugRenderers = List.of(
			sectionRebuildDebugRenderer,
			new DynamicLightLevelDebugRenderer(this),
			new DynamicLightBehaviorDebugRenderer(this, this.dynamicLightSources),
			new DynamicLightSectionDebugRenderer(this)
	);

	private long lastUpdate = System.currentTimeMillis();
	private boolean shouldTick = false;
	boolean shouldForceRefresh = false;
	private int lastUpdateCount = 0;
	private int dynamicLightSourcesCount = 0;

	private LambDynLights() {}

	@Override
	public void onInitializeClient(ModContainer mod) {
		log(LOGGER, "Initializing LambDynamicLights...");

		this.config.load();
		DevModeGui.init();

		CrashReportEvents.CREATE.register((report) -> {
			var category = report.addCategory("Dynamic Lighting");
			category.setDetail("Mode", this.config.getDynamicLightsMode().getName());
			category.setDetail("Dynamic Light Sources", this.dynamicLightSourcesCount);
			category.setDetail(
					"Spatial Hash Occupancy",
					"%d / %d".formatted(this.engine.getLastEntryCount(), DynamicLightingEngine.DEFAULT_LIGHT_SOURCES)
			);
		});

		var platform = YumiMods.get()
				.getEntrypoints(LambDynLightsConstants.NAMESPACE + ":platform_provider", PlatformProvider.class)
				.getFirst()
				.value()
				.getPlatform(mod);

		this.lightSourceApplicationPredicate.set(platform.getLightSourceLoaderApplicationPredicate());
		platform.registerReloader(this.itemLightSources);
		platform.registerReloader(this.entityLightSources);

		platform.getTagLoadedEvent().register(this::onTagsLoaded);

		this.initializeApi();
	}

	/**
	 * Initializes the API.
	 */
	private void initializeApi() {
		this.invokeInitializers(DynamicLightsInitializer.ENTRYPOINT_KEY);
		// Legacy
		this.invokeInitializers("dynamiclights");
	}

	/**
	 * Invokes {@linkplain DynamicLightsInitializer dynamic lights initializers} using Fabric's entrypoint system.
	 *
	 * @param entrypointKey the key of the entrypoints to invoke
	 */
	private void invokeInitializers(String entrypointKey) {
		YumiMods.get().getEntrypoints(entrypointKey, DynamicLightsInitializer.class)
				.stream()
				.map(EntrypointContainer::value)
				.forEach(this::invokeInitializer);
	}

	private void invokeInitializer(DynamicLightsInitializer initializer) {
		initializer.onInitializeDynamicLights(this);
	}

	@Override
	public ItemLightSourceManager itemLightSourceManager() {
		return this.itemLightSources;
	}

	@Override
	public EntityLightSourceManager entityLightSourceManager() {
		return this.entityLightSources;
	}

	@Override
	public DynamicLightBehaviorManager dynamicLightBehaviorManager() {
		return this.dynamicLightBehaviorSources;
	}

	/**
	 * {@return {@code true} if dynamic lighting should tick, or {@code false} otherwise}
	 */
	public boolean shouldTick() {
		return this.shouldTick;
	}

	/**
	 * Returns the last number of dynamic light source updates.
	 *
	 * @return the last number of dynamic light source updates
	 */
	public int getLastUpdateCount() {
		return this.lastUpdateCount;
	}

	public void onTagsLoaded(RegistryAccess registries) {
		this.itemLightSources.apply(registries);
		this.entityLightSources.apply(registries);
	}

	public void onStartLevelTick() {
		var mode = this.config.getDynamicLightsMode();
		boolean shouldTick = mode.isEnabled();

		if (shouldTick && mode.hasDelay()) {
			long currentTime = System.currentTimeMillis();
			if (currentTime < this.lastUpdate + mode.getDelay()) {
				shouldTick = false;
			} else {
				this.lastUpdate = currentTime;
			}
		}

		this.shouldTick = shouldTick || this.shouldForceRefresh;
	}

	public void onEndLevelTick() {
		var renderer = Minecraft.getInstance().levelRenderer;

		this.lightSourcesLock.writeLock().lock();
		if (this.config.getDynamicLightsMode().isEnabled()) {
			Profiler.get().push("dynamic_lighting_compute_spatial_lookup");
			this.engine.computeSpatialLookup(this.dynamicLightSources);
			Profiler.get().pop();
		}
		this.toClear.forEach(source -> {
			source.getDynamicLightChunksToRebuild(true).forEach(chunk -> this.scheduleChunkRebuild(renderer, chunk));
		});
		this.toClear.clear();
		this.lightSourcesLock.writeLock().unlock();

		this.lastUpdateCount = 0;

		if (this.shouldTick) {
			var it = this.dynamicLightSources.iterator();
			while (it.hasNext()) {
				var lightSource = it.next();

				// In case of light sources controlled by a DynamicLightBehavior, they might require polling to be removed.
				if (lightSource instanceof DeferredDynamicLightSource deferred) {
					DynamicLightBehavior behavior = deferred.behavior();

					if (behavior.isRemoved()) {
						this.toClear.add(lightSource);
						it.remove();
						continue;
					}
				}

				var chunks = lightSource.getDynamicLightChunksToRebuild(this.shouldForceRefresh || this.toAdd.contains(lightSource));

				if (!chunks.isEmpty()) {
					chunks.forEach(chunk -> this.scheduleChunkRebuild(renderer, chunk));
					this.lastUpdateCount++;
				}
			}

			this.toAdd.clear();
		}
		this.dynamicLightSourcesCount = this.dynamicLightSources.size();

		this.sectionRebuildDebugRenderer.tick();

		this.shouldForceRefresh = false;
	}

	public void onEndClientTick(Minecraft client) {
		if (TOGGLE_FPS_DYNAMIC_LIGHTING.consumeClick()) {
			boolean newValue = !this.config.getSelfLightSource().get();
			var toggleText = SpruceTexts.getToggleText(newValue);
			this.config.getSelfLightSource().set(newValue);
			this.config.save();

			if (client.player != null) {
				client.player.displayClientMessage(
						Text.translatable(
								LambDynLightsConstants.NAMESPACE + ".key.toggle_fps_dynamic_lighting.info",
								toggleText.copy().withStyle(newValue ? TextFormatting.GREEN : TextFormatting.RED)
						),
						true
				);
			}
		}
	}

	/**
	 * Returns the lightmap with combined light levels.
	 *
	 * @param level the level in which the light is computed
	 * @param pos the position
	 * @param lightmap the vanilla lightmap coordinates
	 * @return the modified lightmap coordinates
	 */
	public int getLightmapWithDynamicLight(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, int lightmap) {
		if (!(level instanceof ClientLevel)) this.lightSourcesLock.readLock().lock();
		double light = this.getDynamicLightLevel(pos);
		if (!(level instanceof ClientLevel)) this.lightSourcesLock.readLock().unlock();
		return this.getLightmapWithDynamicLight(light, lightmap);
	}

	/**
	 * Returns the lightmap with combined light levels.
	 *
	 * @param dynamicLightLevel the dynamic light level
	 * @param lightmap the vanilla lightmap coordinates
	 * @return the modified lightmap coordinates
	 */
	public int getLightmapWithDynamicLight(double dynamicLightLevel, int lightmap) {
		if (dynamicLightLevel > 0) {
			// lightmap is (skyLevel << 20 | blockLevel << 4)

			// Get vanilla block light level.
			int blockLevel = LightTexture.block(lightmap);
			if (dynamicLightLevel > blockLevel) {
				// Equivalent to a << 4 bitshift with a little quirk: this one ensure more precision (more decimals are saved).
				int luminance = (int) (dynamicLightLevel * 16.0);
				lightmap &= 0xfff00000;
				lightmap |= luminance & 0x000fffff;
			}
		}

		return lightmap;
	}

	/**
	 * Returns the dynamic light level at the specified position.
	 *
	 * @param pos the position
	 * @return the dynamic light level at the specified position
	 */
	public double getDynamicLightLevel(@NotNull BlockPos pos) {
		return this.engine.getDynamicLightLevel(pos);
	}

	/**
	 * Adds the light source to the tracked light sources.
	 *
	 * @param lightSource the light source to add
	 */
	public void addLightSource(@NotNull DynamicLightSource lightSource) {
		if (this.containsLightSource(lightSource))
			return;
		this.dynamicLightSources.add(lightSource);
		this.toAdd.add(lightSource);
	}

	/**
	 * Returns whether the light source is tracked or not.
	 *
	 * @param lightSource the light source to check
	 * @return {@code true} if the light source is tracked, else {@code false}
	 */
	public boolean containsLightSource(@NotNull DynamicLightSource lightSource) {
		return this.dynamicLightSources.contains(lightSource);
	}

	/**
	 * Returns the number of dynamic light sources registered.
	 *
	 * @return the number of dynamic light sources emitting light
	 */
	public int getLightSourcesCount() {
		return this.dynamicLightSources.size();
	}

	/**
	 * Removes the light source from the tracked light sources.
	 *
	 * @param lightSource the light source to remove
	 */
	public void removeLightSource(@NotNull EntityDynamicLightSourceBehavior lightSource) {
		var chunkProviders = this.dynamicLightSources.iterator();
		DynamicLightSource it;
		while (chunkProviders.hasNext()) {
			it = chunkProviders.next();
			if (it.equals(lightSource)) {
				chunkProviders.remove();
				this.toClear.add(lightSource);
				break;
			}
		}
	}

	/**
	 * Clears light sources.
	 */
	public void onChangeWorld() {
		var chunkProviders = this.dynamicLightSources.iterator();
		DynamicLightSource it;
		while (chunkProviders.hasNext()) {
			it = chunkProviders.next();
			chunkProviders.remove();
			if (it instanceof EntityDynamicLightSource entityIt && entityIt.getLuminance() > 0)
				entityIt.resetDynamicLight();
			this.toClear.add(it);
		}

		this.engine.resetSize();
	}

	/**
	 * Removes light sources if the filter matches.
	 *
	 * @param filter the removal filter
	 */
	public boolean removeLightSources(@NotNull Predicate<DynamicLightSource> filter) {
		boolean result = false;

		var dynamicLightSources = this.dynamicLightSources.iterator();
		DynamicLightSource it;
		while (dynamicLightSources.hasNext()) {
			it = dynamicLightSources.next();
			if (filter.test(it)) {
				dynamicLightSources.remove();
				this.toClear.add(it);
				result = true;

				if (it instanceof EntityDynamicLightSourceBehavior lightSource) {
					if (lightSource.getLuminance() > 0) {
						lightSource.resetDynamicLight();
					}
				}
			}
		}

		return result;
	}

	public boolean canLightParticle(Particle particle) {
		if (particle instanceof FireflyParticle)
			return this.config.getFireflyLighting().get();
		else if (particle instanceof SonicBoomParticle)
			return true;
		else
			return false;
	}

	/**
	 * Logs an informational message.
	 *
	 * @param logger the logger to use
	 * @param msg the message to log
	 */
	public static void log(Logger logger, String msg) {
		if (!YumiMods.get().isDevelopmentEnvironment()) {
			msg = "[LambDynLights] " + msg;
		}

		logger.info(msg);
	}

	/**
	 * Logs an informational message.
	 *
	 * @param logger the logger to use
	 * @param msg the message to log
	 */
	public static void info(Logger logger, String msg, Object... args) {
		if (!YumiMods.get().isDevelopmentEnvironment()) {
			msg = "[LambDynLights] " + msg;
		}

		logger.info(msg, args);
	}

	/**
	 * Logs a warning message.
	 *
	 * @param logger the logger to use
	 * @param msg the message to log
	 */
	public static void warn(Logger logger, String msg) {
		if (!YumiMods.get().isDevelopmentEnvironment()) {
			msg = "[LambDynLights] " + msg;
		}

		logger.warn(msg);
	}

	/**
	 * Logs a warning message.
	 *
	 * @param logger the logger to use
	 * @param msg the message to log
	 */
	public static void warn(Logger logger, String msg, Object... args) {
		if (!YumiMods.get().isDevelopmentEnvironment()) {
			msg = "[LambDynLights] " + msg;
		}

		logger.warn(msg, args);
	}

	/**
	 * Logs an error message.
	 *
	 * @param logger the logger to use
	 * @param msg the message to log
	 */
	public static void error(Logger logger, String msg, Object... args) {
		if (!YumiMods.get().isDevelopmentEnvironment()) {
			msg = "[LambDynLights] " + msg;
		}

		logger.error(msg, args);
	}

	/**
	 * Schedules a chunk rebuild at the specified chunk position.
	 *
	 * @param renderer the renderer
	 * @param chunkPos the packed chunk position
	 */
	private void scheduleChunkRebuild(@NotNull LevelRenderer renderer, long chunkPos) {
		scheduleChunkRebuild(renderer, ChunkSectionPos.x(chunkPos), ChunkSectionPos.y(chunkPos), ChunkSectionPos.z(chunkPos));
		this.sectionRebuildDebugRenderer.scheduleChunkRebuild(chunkPos);
	}

	public static void scheduleChunkRebuild(@NotNull LevelRenderer renderer, int x, int y, int z) {
		if (Minecraft.getInstance().level != null)
			((LevelRendererAccessor) renderer).lambdynlights$scheduleChunkRebuild(x, y, z, false);
	}

	/**
	 * Updates the dynamic lights tracking.
	 *
	 * @param lightSource the light source
	 */
	public static void updateTracking(@NotNull EntityDynamicLightSourceBehavior lightSource) {
		boolean enabled = lightSource.isDynamicLightEnabled();
		int luminance = lightSource.getLuminance();

		if (!enabled && luminance > 0) {
			lightSource.setDynamicLightEnabled(true);
		} else if (enabled && luminance < 1) {
			lightSource.setDynamicLightEnabled(false);
		}
	}

	private static boolean isEyeSubmergedInFluid(LivingEntity entity) {
		if (!LambDynLights.get().config.getWaterSensitiveCheck().get()) {
			return false;
		}

		var eyePos = BlockPos.ofFloored(entity.getX(), entity.getEyeY(), entity.getZ());
		return !entity.level().getFluidState(eyePos).isEmpty();
	}

	public static int getLivingEntityLuminanceFromItems(LivingEntity entity) {
		boolean submergedInFluid = isEyeSubmergedInFluid(entity);
		int luminance = 0;

		for (var equipmentSlot : EquipmentSlot.VALUES) {
			var equipped = entity.getItemBySlot(equipmentSlot);

			if (!equipped.isEmpty()) {
				luminance = Math.max(luminance, INSTANCE.itemLightSources.getLuminance(equipped, submergedInFluid));
			}
		}

		if (luminance < 15) {
			for (var compat : CompatLayer.LAYERS) {
				luminance = Math.max(luminance, compat.getLivingEntityLuminanceFromItems(INSTANCE.itemLightSources, entity, submergedInFluid));

				if (luminance == 15) {
					break;
				}
			}
		}

		return luminance;
	}

	/**
	 * Returns the luminance from an entity.
	 *
	 * @param entity the entity
	 * @param <T> the type of the entity
	 * @return the luminance
	 */
	public static <T extends Entity> int getLuminanceFrom(T entity) {
		if (!INSTANCE.config.getEntitiesLightSource().get())
			return 0;
		if (entity == Minecraft.getInstance().player && !INSTANCE.config.getSelfLightSource().get())
			return 0;

		if (!DynamicLightingEngine.canLightUp(entity))
			return 0;

		return INSTANCE.entityLightSources.getLuminance(entity);
	}

	/**
	 * Returns the LambDynamicLights mod instance.
	 *
	 * @return the mod instance
	 */
	public static LambDynLights get() {
		return INSTANCE;
	}

	/**
	 * {@return a LambDynamicLights identifier}
	 *
	 * @param path the path
	 */
	public static Identifier id(String path) {
		return Identifier.of(LambDynLightsConstants.NAMESPACE, path);
	}
}
