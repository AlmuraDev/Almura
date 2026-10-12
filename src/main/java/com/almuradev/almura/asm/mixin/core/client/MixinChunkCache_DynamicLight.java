/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.client;

import com.almuradev.almura.feature.armor.client.ClientDynamicLightManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client side dynamic light for blocks: chunk sections are rendered from a ChunkCache, so when a block's light is read while building a
 * section, include light from players wearing a lighted helmet (see {@link ClientDynamicLightManager}). Smooth lighting reads neighbours
 * through the same method, so it stays consistent. Only client worlds are affected, and nothing is written to the world.
 */
@Mixin(ChunkCache.class)
public abstract class MixinChunkCache_DynamicLight {

    @Shadow protected World world;

    @Inject(method = "getCombinedLight", at = @At("RETURN"), cancellable = true)
    private void onGetCombinedLight(final BlockPos pos, final int lightValue, final CallbackInfoReturnable<Integer> cir) {
        if (this.world != null && this.world.isRemote) {
            cir.setReturnValue(ClientDynamicLightManager.withDynamicLight(cir.getReturnValueI(), pos));
        }
    }
}
