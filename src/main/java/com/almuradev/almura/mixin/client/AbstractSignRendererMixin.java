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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A sign's text past Almura's sign text render distance isn't drawn (the sign still is), as 3.5's {@code MixinTileEntitySignRenderer}.
 */
@Mixin(AbstractSignRenderer.class)
public abstract class AbstractSignRendererMixin {

    @Inject(method = "submitSignText", at = @At("HEAD"), cancellable = true)
    private void almura$signTextDistance(final SignRenderState state, final PoseStack pose, final SubmitNodeCollector collector, final SignText text,
        final CallbackInfo callback) {
        if (state.blockPos != null) {
            final Vec3 centre = Vec3.atCenterOf(state.blockPos);
            if (RenderDistances.beyond(AlmuraClientConfig.SIGN_TEXT_RENDER_DISTANCE, centre.x, centre.y, centre.z)) {
                callback.cancel();
            }
        }
    }
}
