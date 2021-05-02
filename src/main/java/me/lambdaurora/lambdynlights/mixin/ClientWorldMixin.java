/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin;

import me.lambdaurora.lambdynlights.DynamicLightSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin
{
    @Shadow
    protected EntityLookup<Entity> getEntityLookup() {
        return null;
    }

    @Inject(method = "removeEntity(ILnet/minecraft/entity/Entity$RemovalReason;)V", at = @At("HEAD"))
    private void onFinishRemovingEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci)
    {
        Entity entity2 = (Entity)this.getEntityLookup().get(entityId);
        if(entity2 != null) {
            DynamicLightSource dls = (DynamicLightSource) entity2;
            dls.setDynamicLightEnabled(false);
        }
    }
}
