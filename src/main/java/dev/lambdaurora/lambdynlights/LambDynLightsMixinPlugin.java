/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * LambDynamicLights mixin plugin for conditional mixins.
 *
 * @author LambdAurora
 * @version 3.1.0
 * @since 1.0.0
 */
public class LambDynLightsMixinPlugin implements IMixinConfigPlugin {
	private final Object2BooleanMap<String> conditionalMixins = new Object2BooleanOpenHashMap<>();

	public LambDynLightsMixinPlugin() {
		this.conditionalMixins.put("dev.lambdaurora.lambdynlights.mixin.DevModeMixin", LambDynLightsConstants.isDevMode());
		this.conditionalMixins.put("dev.lambdaurora.lambdynlights.mixin.sodium.SodiumOptionsGuiMixin", LambDynLightsCompat.isSodiumInstalled());
	}

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return this.conditionalMixins.getOrDefault(mixinClassName, true);
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
