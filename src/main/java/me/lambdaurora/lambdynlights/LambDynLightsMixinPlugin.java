/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * LambDynamicLights mixin plugin for conditional mixins.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
public class LambDynLightsMixinPlugin implements IMixinConfigPlugin
{
    private final HashMap<String, Boolean> conditionalMixins = new HashMap<>();

    public LambDynLightsMixinPlugin()
    {
        boolean ltrInstalled = LambDynLightsCompat.isLilTaterReloadedInstalled();
        this.conditionalMixins.put("me.lambdaurora.lambdynlights.mixin.ltr.LilTaterBlocksMixin", ltrInstalled);
        this.conditionalMixins.put("me.lambdaurora.lambdynlights.mixin.ltr.LilTaterBlockEntityMixin", ltrInstalled);

        boolean canvasInstalled = LambDynLightsCompat.isCanvasInstalled();
        boolean sodiumInstalled = LambDynLightsCompat.isSodiumInstalled();
        this.conditionalMixins.put("me.lambdaurora.lambdynlights.mixin.CanvasWorldRendererMixin", canvasInstalled);
        this.conditionalMixins.put("me.lambdaurora.lambdynlights.mixin.EntityLighterMixin", sodiumInstalled);
        this.conditionalMixins.put("me.lambdaurora.lambdynlights.mixin.SodiumWorldRendererMixin", sodiumInstalled);
        this.conditionalMixins.put("me.lambdaurora.lambdynlights.mixin.WorldRendererMixin", !sodiumInstalled && !canvasInstalled);
    }

    @Override
    public void onLoad(String mixinPackage)
    {
    }

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        return this.conditionalMixins.getOrDefault(mixinClassName, Boolean.TRUE);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
    {
    }

    @Override
    public List<String> getMixins()
    {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {
    }
}
