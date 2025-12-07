/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.engine.source.DeferredDynamicLightSource;
import dev.lambdaurora.lambdynlights.engine.source.DynamicLightSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.Set;

/**
 * Represents a debug renderer for the bounding boxes of {@link DynamicLightBehavior}.
 *
 * @author Akarys
 * @version 4.0.0
 * @since 4.0.0
 */
@Environment(EnvType.CLIENT)
public class DynamicLightBehaviorDebugRenderer extends DynamicLightDebugRenderer {
	private final Set<DynamicLightSource> lightSourceSetRef;

	public DynamicLightBehaviorDebugRenderer(LambDynLights mod, Set<DynamicLightSource> lightSourceSetRef) {
		super(mod);
		this.lightSourceSetRef = lightSourceSetRef;
	}

	private boolean isEnabled() {
		return this.config.getDebugDisplayHandlerBoundingBox().get();
	}

	@Override
	public void render(
			PoseStack poses, MultiBufferSource bufferSource, double x, double y, double z
	) {
		if (!this.isEnabled()) {
			return;
		}

		poses.pushPose();
		poses.translate(-x, -y, -z);
		this.lightSourceSetRef.forEach(lightSource -> {
			if (lightSource instanceof DeferredDynamicLightSource deferredLightSource) {
				VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

				DynamicLightBehavior.BoundingBox boundingBox = deferredLightSource.behavior().getBoundingBox();

				LevelRenderer.renderLineBox(
						poses, vertexConsumer,
						boundingBox.startX(), boundingBox.startY(), boundingBox.startZ(),
						boundingBox.endX(), boundingBox.endY(), boundingBox.endZ(),
						1.f, 0.f, 0.f, 0.8f
				);
			}
		});
		poses.popPose();
	}
}
