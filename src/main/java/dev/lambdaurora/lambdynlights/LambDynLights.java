/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
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
import dev.lambdaurora.lambdynlights.mixin.LevelRendererAccessor;
import dev.lambdaurora.lambdynlights.resource.entity.EntityLightSources;
import dev.lambdaurora.lambdynlights.resource.item.ItemLightSources;
import dev.lambdaurora.lambdynlights.util.DynamicLightBehaviorDebugRenderer;
import dev.lambdaurora.lambdynlights.util.DynamicLightDebugRenderer;
import dev.lambdaurora.lambdynlights.util.DynamicLightLevelDebugRenderer;
import dev.lambdaurora.lambdynlights.util.DynamicLightSectionDebugRenderer;
import dev.yumi.mc.core.api.CrashReportEvents;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.YumiMods;
import dev.yumi.mc.core.api.entrypoint.EntrypointContainer;
import dev.yumi.mc.core.api.entrypoint.client.ClientModInitializer;
import dev.yumi.mc.core.api.metadata.ManifestCustomValue;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.ChunkSectionPos;
import net.minecraft.resources.io.ResourceType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
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
 * @version 4.1.6
 * @since 1.0.0
 */
@ApiStatus.Internal
public class LambDynLights implements ClientModInitializer, DynamicLightsContext {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambDynamicLights");
	private static LambDynLights INSTANCE;

	public final DynamicLightsConfig config = new DynamicLightsConfig(this);
	private final ItemLightSources itemLightSources = new ItemLightSources();
	private final EntityLightSources entityLightSources = new EntityLightSources(this.itemLightSources);
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

	@SuppressWarnings("removal")
	@Override
	public void onInitializeClient(ModContainer mod) {
		INSTANCE = this;
		log(LOGGER, "Initializing LambDynamicLights...");

		this.config.load();

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this.itemLightSources);
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this.entityLightSources);

		CrashReportEvents.CREATE.register((report) -> {
			var category = report.addCategory("Dynamic Lighting");
			category.setDetail("Mode", this.config.getDynamicLightsMode().getName());
			category.setDetail("Dynamic Light Sources", this.dynamicLightSourcesCount);
			category.setDetail(
					"Spatial Hash Occupancy",
					"%d / %d".formatted(this.engine.getLastEntryCount(), DynamicLightingEngine.MAX_LIGHT_SOURCES)
			);
		});

		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
			this.itemLightSources.apply(registries);
			this.entityLightSources.apply(registries);
		});

		ClientTickEvents.START_WORLD_TICK.register(level -> {
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
		});

		ClientTickEvents.END_WORLD_TICK.register(level -> {
			var renderer = Minecraft.getInstance().levelRenderer;

			this.lightSourcesLock.writeLock().lock();
			if (this.config.getDynamicLightsMode().isEnabled()) {
				level.getProfiler().push("dynamic_lighting_compute_spatial_lookup");
				this.engine.computeSpatialLookup(this.dynamicLightSources);
				level.getProfiler().pop();
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
		});

		this.initializeApi();
		DynamicLightHandlers.registerDefaultHandlers();
	}

	/**
	 * Initializes the API.
	 */
	private void initializeApi() {
		this.invokeInitializers(DynamicLightsInitializer.ENTRYPOINT_KEY);
		// Legacy
		this.invokeInitializers("dynamiclights");

		// Sinytra
		// Under NeoForge there is no simple entrypoint system, so we end up just re-implementing Fabric-style entrypoints.
		YumiMods.get().getMods().stream()
				.filter(mod -> mod.getCustomProperties().get(DynamicLightsInitializer.ENTRYPOINT_KEY) != null)
				.forEach(this::invokeInitializer);
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

	private void invokeInitializer(ModContainer mod) {
		String id = mod.id();
		var entrypointValue = mod.getCustomProperties().get(DynamicLightsInitializer.ENTRYPOINT_KEY);

		if (!(entrypointValue instanceof ManifestCustomValue.StringValue(String value))) {
			error(LOGGER, "Ignoring {} entrypoint from mod {}: not a string", DynamicLightsInitializer.ENTRYPOINT_KEY, id);
			return;
		}

		try {
			var fabricMod = FabricLoader.getInstance().getModContainer(id);

			if (fabricMod.isPresent()) {
				var initializer = LanguageAdapter.getDefault().create(fabricMod.get(), value, DynamicLightsInitializer.class);
				this.invokeInitializer(initializer);
			} else {
				warn(LOGGER, "Failed to initialize {} entrypoint from mod {}: could not locate Fabric-specific mod.", DynamicLightsInitializer.ENTRYPOINT_KEY, id);
			}
		} catch (LanguageAdapterException e) {
			error(LOGGER, "Failed to initialize {} entrypoint from mod {}: exception thrown", DynamicLightsInitializer.ENTRYPOINT_KEY, id, e);
			throw new RuntimeException(e);
		}
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
	public void clearLightSources() {
		var chunkProviders = this.dynamicLightSources.iterator();
		DynamicLightSource it;
		while (chunkProviders.hasNext()) {
			it = chunkProviders.next();
			chunkProviders.remove();
			if (it instanceof EntityDynamicLightSource entityIt && entityIt.getLuminance() > 0)
				entityIt.resetDynamicLight();
			this.toClear.add(it);
		}
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

	/**
	 * Removes entities light source from tracked light sources.
	 */
	public void removeEntitiesLightSource() {
		this.removeLightSources(lightSource -> (lightSource instanceof Entity && !(lightSource instanceof Player)));
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

		for (var equipped : entity.getAllSlots()) {
			if (!equipped.isEmpty())
				luminance = Math.max(luminance, INSTANCE.itemLightSources.getLuminance(equipped, submergedInFluid));
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
	 * {@return the luminance value of the item stack}
	 *
	 * @param stack the item stack
	 * @param submergedInWater {@code true} if the stack is submerged in water, else {@code false}
	 * @return the luminance
	 * @deprecated You already shouldn't use this as this is not part of the API, but just in case:
	 * use {@link ItemLightSourceManager#getLuminance(ItemStack, boolean)} instead.
	 * <p>
	 * This is fully removed in LambDynamicLights releases targeting Minecraft 1.21.4 and newer.
	 */
	@Deprecated(forRemoval = true)
	@ApiStatus.ScheduledForRemoval(inVersion = "4.0.0+1.21.4")
	public static int getLuminanceFromItemStack(@NotNull ItemStack stack, boolean submergedInWater) {
		return INSTANCE.itemLightSources.getLuminance(stack, submergedInWater);
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
}
