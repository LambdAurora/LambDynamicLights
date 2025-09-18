/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSourceBehavior;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	@Inject(
			method = "tickNonPassenger",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V", shift = At.Shift.AFTER)
	)
	private void lambdynlights$onTickNonPassenger(Entity entity, CallbackInfo ci) {
		EntityDynamicLightSourceBehavior.tickEntity(entity);
	}

	@Inject(
			method = "tickPassenger",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V", shift = At.Shift.AFTER)
	)
	private void lambdynlights$onTickPassenger(Entity vehicle, Entity passenger, CallbackInfo ci) {
		EntityDynamicLightSourceBehavior.tickEntity(passenger);
	}

	@Inject(method = "removeEntity(ILnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At("RETURN"))
	private void lambdynlights$onFinishRemovingEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci, @Local Entity entity) {
		if (entity != null) {
			var dls = (EntityDynamicLightSourceBehavior) entity;
			dls.setDynamicLightEnabled(false);
		}
	}
}
