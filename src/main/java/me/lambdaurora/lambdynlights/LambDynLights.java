/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.lambdaurora.lambdynlights.accessor.WorldRendererAccessor;
import me.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import me.lambdaurora.lambdynlights.api.item.ItemLightSources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

/**
 * Represents the LambDynamicLights mod.
 *
 * @author LambdAurora
 * @version 1.3.1
 * @since 1.0.0
 */
public class LambDynLights implements ClientModInitializer
{
    public static final  String                                    MODID               = "lambdynlights";
    private static final double                                    MAX_RADIUS          = 7.75;
    private static       LambDynLights                             INSTANCE;
    public final         Logger                                    logger              = LogManager.getLogger(MODID);
    public final         DynamicLightsConfig                       config              = new DynamicLightsConfig(this);
    private final        ConcurrentLinkedQueue<DynamicLightSource> dynamicLightSources = new ConcurrentLinkedQueue<>();
    private              long                                      lastUpdate          = System.currentTimeMillis();
    private              boolean                                   notifiedFirstTime   = false;

    @Override
    public void onInitializeClient()
    {
        INSTANCE = this;
        this.log("Initializing LambDynamicLights...");

        this.config.load();

        ClientTickEvents.START_WORLD_TICK.register(world -> {
            if (!this.notifiedFirstTime && this.config.isFirstTime()) {
                this.notifiedFirstTime = true;

                MinecraftClient client = MinecraftClient.getInstance();
                client.getToastManager().add(SystemToast.create(client,
                        SystemToast.Type.TUTORIAL_HINT,
                        new LiteralText("LambDynamicLights").formatted(Formatting.GOLD),
                        new TranslatableText("lambdynlights.toast.first_time")));
            }
        });

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener()
        {
            @Override
            public Identifier getFabricId()
            {
                return new Identifier(MODID, "dynamiclights_resources");
            }

            @Override
            public void apply(ResourceManager manager)
            {
                ItemLightSources.load(manager);
            }
        });

        DynamicLightHandlers.registerDefaultHandlers();
    }

    /**
     * Updates all light sources.
     *
     * @param renderer The renderer.
     */
    public void updateAll(@NotNull WorldRenderer renderer)
    {
        if (!this.config.getDynamicLightsMode().isEnabled())
            return;

        long now = System.currentTimeMillis();
        if (now >= this.lastUpdate + 50) {
            this.lastUpdate = now;

            for (DynamicLightSource lightSource : this.dynamicLightSources) {
                lightSource.lambdynlights_updateDynamicLight(renderer);
            }
        }
    }

    /**
     * Returns the lightmap with combined light levels.
     *
     * @param pos      The position.
     * @param lightmap The vanilla lightmap.
     * @return The modified lightmap.
     */
    public int getLightmapWithDynamicLight(@NotNull BlockPos pos, int lightmap)
    {
        return this.getLightmapWithDynamicLight(this.getDynamicLightLevel(pos), lightmap);
    }

    /**
     * Returns the lightmap with combined light levels.
     *
     * @param entity   The entity.
     * @param lightmap The vanilla lightmap.
     * @return The
     */
    public int getLightmapWithDynamicLight(@NotNull Entity entity, int lightmap)
    {
        int posLightLevel = (int) this.getDynamicLightLevel(entity.getBlockPos());
        int entityLuminance = ((DynamicLightSource) entity).getLuminance();

        return this.getLightmapWithDynamicLight(Math.max(posLightLevel, entityLuminance), lightmap);
    }

    /**
     * Returns the lightmap with combined light levels.
     *
     * @param dynamicLightLevel The dynamic light level.
     * @param lightmap          The vanilla lightmap.
     * @return The modified lightmap.
     */
    public int getLightmapWithDynamicLight(double dynamicLightLevel, int lightmap)
    {
        if (dynamicLightLevel > 0) {
            // lightmap is (skyLevel << 20 | blockLevel << 4)

            // Get vanilla block light level.
            int blockLevel = LightmapTextureManager.getBlockLightCoordinates(lightmap);
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
     * @param pos The position.
     * @return The dynamic light level at the spec
     */
    public double getDynamicLightLevel(@NotNull BlockPos pos)
    {
        double result = 0;
        for (DynamicLightSource lightSource : this.dynamicLightSources) {
            result = maxDynamicLightLevel(pos, lightSource, result);
        }

        return MathHelper.clamp(result, 0, 15);
    }

    /**
     * Returns the dynamic light level generated by the light source at the specified position.
     *
     * @param pos               The position.
     * @param lightSource       The light source.
     * @param currentLightLevel The current surrounding dynamic light level.
     * @return The dynamic light level.
     */
    public static double maxDynamicLightLevel(@NotNull BlockPos pos, @NotNull DynamicLightSource lightSource, double currentLightLevel)
    {
        int luminance = lightSource.getLuminance();
        if (luminance > 0) {
            // Can't use Entity#squaredDistanceTo because of eye Y coordinate.
            double dx = pos.getX() - lightSource.getDynamicLightX() + 0.5;
            double dy = pos.getY() - lightSource.getDynamicLightY() + 0.5;
            double dz = pos.getZ() - lightSource.getDynamicLightZ() + 0.5;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            // 7.75 because else we would have to update more chunks and that's not a good idea.
            // 15 (max range for blocks) would be too much and a bit cheaty.
            if (distance <= MAX_RADIUS) {
                double multiplier = 1.0 - distance / MAX_RADIUS;
                double lightLevel = multiplier * (double) luminance;
                if (lightLevel > currentLightLevel) {
                    currentLightLevel = lightLevel;
                }
            }
        }
        return currentLightLevel;
    }

    /**
     * Returns the dynamic luminance at the specified position.
     *
     * @param pos The position.
     * @return The dynamic luminance.
     */
    public int getDynamicLuminanceAt(@NotNull BlockPos pos)
    {
        return 0;
    }

    /**
     * Adds the light source to the tracked light sources.
     *
     * @param lightSource The light source to add.
     */
    public void addLightSource(@NotNull DynamicLightSource lightSource)
    {
        if (!lightSource.getDynamicLightWorld().isClient())
            return;
        if (!this.config.getDynamicLightsMode().isEnabled())
            return;
        if (this.containsLightSource(lightSource))
            return;
        this.dynamicLightSources.add(lightSource);
    }

    /**
     * Returns whether the light source is tracked or not.
     *
     * @param lightSource The light source to check.
     * @return True if the light source is tracked, else false.
     */
    public boolean containsLightSource(@NotNull DynamicLightSource lightSource)
    {
        if (!lightSource.getDynamicLightWorld().isClient())
            return false;
        return this.dynamicLightSources.contains(lightSource);
    }

    /**
     * Removes the light source from the tracked light sources.
     *
     * @param lightSource The light source to remove.
     */
    public void removeLightSource(@NotNull DynamicLightSource lightSource)
    {
        Iterator<DynamicLightSource> dynamicLightSources = this.dynamicLightSources.iterator();
        DynamicLightSource it;
        while (dynamicLightSources.hasNext()) {
            it = dynamicLightSources.next();
            if (it.equals(lightSource)) {
                dynamicLightSources.remove();
                if (MinecraftClient.getInstance().worldRenderer != null)
                    lightSource.lambdynlights_scheduleTrackedChunksRebuild(MinecraftClient.getInstance().worldRenderer);
                break;
            }
        }
    }

    /**
     * Clears light sources.
     */
    public void clearLightSources()
    {
        Iterator<DynamicLightSource> dynamicLightSources = this.dynamicLightSources.iterator();
        DynamicLightSource it;
        while (dynamicLightSources.hasNext()) {
            it = dynamicLightSources.next();
            dynamicLightSources.remove();
            if (MinecraftClient.getInstance().worldRenderer != null)
                it.lambdynlights_scheduleTrackedChunksRebuild(MinecraftClient.getInstance().worldRenderer);
            break;
        }
    }

    /**
     * Removes light sources if the filter matches.
     *
     * @param filter Filter.
     */
    public void removeLightSources(@NotNull Predicate<DynamicLightSource> filter)
    {
        Iterator<DynamicLightSource> dynamicLightSources = this.dynamicLightSources.iterator();
        DynamicLightSource it;
        while (dynamicLightSources.hasNext()) {
            it = dynamicLightSources.next();
            if (filter.test(it)) {
                dynamicLightSources.remove();
                if (MinecraftClient.getInstance().worldRenderer != null)
                    it.lambdynlights_scheduleTrackedChunksRebuild(MinecraftClient.getInstance().worldRenderer);
                break;
            }
        }
    }

    /**
     * Removes entities light source from tracked light sources.
     */
    public void removeEntitiesLightSource()
    {
        this.removeLightSources(lightSource -> (lightSource instanceof Entity && !(lightSource instanceof PlayerEntity)));
    }

    /**
     * Removes Creeper light sources from tracked light sources.
     */
    public void removeCreeperLightSources()
    {
        this.removeLightSources(entity -> entity instanceof CreeperEntity);
    }

    /**
     * Removes TNT light sources from tracked light sources.
     */
    public void removeTntLightSources()
    {
        this.removeLightSources(entity -> entity instanceof TntEntity);
    }

    /**
     * Removes block entities light source from tracked light sources.
     */
    public void removeBlockEntitiesLightSource()
    {
        this.removeLightSources(lightSource -> lightSource instanceof BlockEntity);
    }

    /**
     * Prints a message to the terminal.
     *
     * @param info The message to print.
     */
    public void log(String info)
    {
        this.logger.info("[LambDynLights] " + info);
    }

    /**
     * Prints a warning message to the terminal.
     *
     * @param info The message to print.
     */
    public void warn(String info)
    {
        this.logger.warn("[LambDynLights] " + info);
    }

    /**
     * Schedules a chunk rebuild at the specified chunk position.
     *
     * @param renderer The renderer.
     * @param chunkPos The chunk position.
     */
    public static void scheduleChunkRebuild(@NotNull WorldRenderer renderer, @NotNull BlockPos chunkPos)
    {
        ((WorldRendererAccessor) renderer).lambdynlights_scheduleChunkRebuild(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), false);
    }

    /**
     * Updates the tracked chunk sets.
     *
     * @param chunkPos The chunk position.
     * @param old      The set of old chunk coordinates to remove this chunk from it.
     * @param newPos   The set of new chunk coordinates to add this chunk to it.
     */
    public static void updateTrackedChunks(@NotNull BlockPos chunkPos, @Nullable LongOpenHashSet old, @Nullable LongOpenHashSet newPos)
    {
        if (old != null || newPos != null) {
            long pos = chunkPos.asLong();
            if (old != null)
                old.remove(pos);
            if (newPos != null)
                newPos.add(pos);
        }
    }

    /**
     * Updates the dynamic lights tracking.
     *
     * @param lightSource The light source.
     */
    public static void updateTracking(@NotNull DynamicLightSource lightSource)
    {
        if (!lightSource.isDynamicLightEnabled() && lightSource.getLuminance() > 0) {
            lightSource.setDynamicLightEnabled(true);
        } else if (lightSource.isDynamicLightEnabled() && lightSource.getLuminance() < 1) {
            lightSource.setDynamicLightEnabled(false);
        }
    }

    /**
     * Returns the luminance from an item stack.
     *
     * @param stack            The item stack.
     * @param submergedInWater True if the stack is submerged in water, else false.
     * @return The luminance of the item.
     */
    public static int getLuminanceFromItemStack(@NotNull ItemStack stack, boolean submergedInWater)
    {
        return ItemLightSources.getLuminance(stack, submergedInWater);
    }

    /**
     * Returns the LambDynamicLights mod instance.
     *
     * @return The mod instance.
     */
    public static LambDynLights get()
    {
        return INSTANCE;
    }
}
