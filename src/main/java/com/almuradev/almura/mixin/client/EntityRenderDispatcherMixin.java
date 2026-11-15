/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import com.almuradev.almura.feature.renderdistance.RenderDistances;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Item frames (and glow item frames) past Almura's item frame render distance aren't drawn, as 3.5's {@code MixinRenderItemFrame}.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDDF)Z", at = @At("HEAD"),
        cancellable = true)
    private void almura$itemFrameDistance(final Entity entity, final Frustum frustum, final double cameraX, final double cameraY,
        final double cameraZ, final float partialTick, final CallbackInfoReturnable<Boolean> callback) {
        if (entity instanceof ItemFrame && RenderDistances.beyond(AlmuraClientConfig.ITEM_FRAME_RENDER_DISTANCE,
            entity.distanceToSqr(cameraX, cameraY, cameraZ))) {
            callback.setReturnValue(false);
        }
    }
}
