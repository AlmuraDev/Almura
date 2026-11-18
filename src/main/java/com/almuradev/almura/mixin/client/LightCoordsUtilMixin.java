/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import com.almuradev.almura.feature.armor.client.HelmetLights;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The lighted helmet's light where the client reads light to draw blocks, block entities, item frames and the like (as 3.5's
 * {@code MixinChunkCache_DynamicLight}): the brighter of the real block light and the helmet's ({@link HelmetLights}). Only for what the
 * client draws (its world, and the regions its sections are built from), never the server's reads in singleplayer.
 */
@Mixin(LightCoordsUtil.class)
public abstract class LightCoordsUtilMixin {

    @Inject(method = "getLightCoords(Lnet/minecraft/util/LightCoordsUtil$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;"
        + "Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At("RETURN"), cancellable = true)
    private static void almura$helmetLight(final LightCoordsUtil.BrightnessGetter getter, final BlockAndLightGetter level, final BlockState state,
        final BlockPos pos, final CallbackInfoReturnable<Integer> cir) {
        if (level instanceof RenderSectionRegion || level instanceof ClientLevel) {
            cir.setReturnValue(HelmetLights.withHelmetLight(cir.getReturnValueI(), pos));
        }
    }
}
