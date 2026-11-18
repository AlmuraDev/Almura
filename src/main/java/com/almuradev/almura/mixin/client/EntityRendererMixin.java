/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import com.almuradev.almura.feature.armor.client.HelmetLights;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The lighted helmet's light on entities (as 3.5's {@code MixinWorld_DynamicLight} lit them): the brighter of their block light and the
 * helmet's ({@link HelmetLights}). Renderers that light their entity themselves (a blaze, a glow squid) keep theirs.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "getBlockLightLevel", at = @At("RETURN"), cancellable = true)
    private void almura$helmetLight(final Entity entity, final BlockPos pos, final CallbackInfoReturnable<Integer> cir) {
        final int helmet = HelmetLights.lightAt(pos);
        if (helmet > cir.getReturnValueI()) {
            cir.setReturnValue(helmet);
        }
    }
}
