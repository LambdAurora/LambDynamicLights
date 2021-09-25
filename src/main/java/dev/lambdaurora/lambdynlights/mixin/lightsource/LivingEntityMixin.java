/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin.lightsource;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;

import dev.lambdaurora.lambdynlights.DynamicLightSource;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements DynamicLightSource {
    @Unique
    private int luminance;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void dynamicLightTick() {
        if (this.isOnFire() || this.isGlowing()) {
            this.luminance = 15;
        } else {
            int luminance = 0;
            var eyePos = new BlockPos(this.getX(), this.getEyeY(), this.getZ());
            boolean submergedInFluid = !this.world.getFluidState(eyePos).isEmpty();
            for (var equipped : this.getItemsEquipped()) {
                if (!equipped.isEmpty())
                    luminance = Math.max(luminance, LambDynLights.getLuminanceFromItemStack(equipped, submergedInFluid));
            }
            
            if(LambDynLightsCompat.isTrinketsInstalled()) {
				List<Pair<SlotReference, ItemStack>> trinkets = TrinketsApi.getTrinketComponent((LivingEntity) (Object) this).get().getAllEquipped();

				for(Pair<SlotReference, ItemStack> equipped : trinkets) {
					ItemStack stack = equipped.getRight();

					if(stack != null && stack.getItem() != null) {
						if(!stack.isEmpty())
							luminance = Math.max(luminance, LambDynLights.getLuminanceFromItemStack(stack, submergedInFluid));
					}
				}
			}

            this.luminance = luminance;
        }

        int luminance = DynamicLightHandlers.getLuminanceFrom(this);
        if (luminance > this.luminance)
            this.luminance = luminance;

        if (!LambDynLights.get().config.hasEntitiesLightSource() && this.getType() != EntityType.PLAYER)
            this.luminance = 0;
    }

    @Override
    public int getLuminance() {
        return this.luminance;
    }
}
