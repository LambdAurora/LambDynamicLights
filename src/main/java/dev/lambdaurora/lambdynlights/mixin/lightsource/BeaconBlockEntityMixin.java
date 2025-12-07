/*
 * Copyright © 2024 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.lightsource;

import com.llamalad7.mixinextras.sugar.Local;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.behavior.BeaconLightBehavior;
import dev.lambdaurora.lambdynlights.echo.BeaconBlockEntityLightSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalInt;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin extends BlockEntity implements BeaconBlockEntityLightSource {
	@Shadow
	int levels;
	@Unique
	private BeaconLightBehavior dynamicLightBeam;

	public BeaconBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Inject(
			method = "tick",
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;beamSections:Ljava/util/List;", ordinal = 2)
	)
	private static void lambdynlights$onTick(
			Level level, BlockPos pos, BlockState state, BeaconBlockEntity beacon,
			CallbackInfo ci,
			@Local(ordinal = 0) boolean hadLevels
	) {
		if (level.isClientSide()) {
			var specialBeacon = (BeaconBlockEntityLightSource) beacon;

			if (!LambDynLights.get().config.getBeamLighting().get()) {
				if (specialBeacon.lambdynlights$getDynamicLightBeam() != null) {
					LambDynLights.get().dynamicLightBehaviorManager().remove(specialBeacon.lambdynlights$getDynamicLightBeam());
					specialBeacon.lambdynlights$setDynamicLightBeam(null);
				}

				return;
			}

			if (specialBeacon.lambdynlights$getLevels() > 0 && specialBeacon.lambdynlights$getDynamicLightBeam() == null) {
				specialBeacon.lambdynlights$setDynamicLightBeam(new BeaconLightBehavior(
						pos.getX(), OptionalInt.of(pos.getY() + 1), pos.getZ(), state.getLightEmission(), level
				));
				LambDynLights.get().dynamicLightBehaviorManager().add(specialBeacon.lambdynlights$getDynamicLightBeam());
			} else if (hadLevels && specialBeacon.lambdynlights$getLevels() == 0 && specialBeacon.lambdynlights$getDynamicLightBeam() != null) {
				LambDynLights.get().dynamicLightBehaviorManager().remove(specialBeacon.lambdynlights$getDynamicLightBeam());
				specialBeacon.lambdynlights$setDynamicLightBeam(null);
			}
		}
	}

	@Inject(method = "setRemoved", at = @At("RETURN"))
	private void lambdynlights$markRemoved(CallbackInfo ci) {
		if (this.level != null && this.level.isClientSide() && this.dynamicLightBeam != null) {
			LambDynLights.get().dynamicLightBehaviorManager().remove(this.dynamicLightBeam);
		}
	}

	@Override
	public int lambdynlights$getLevels() {
		return this.levels;
	}

	@Override
	public BeaconLightBehavior lambdynlights$getDynamicLightBeam() {
		return this.dynamicLightBeam;
	}

	@Override
	public void lambdynlights$setDynamicLightBeam(BeaconLightBehavior beam) {
		this.dynamicLightBeam = beam;
	}
}
