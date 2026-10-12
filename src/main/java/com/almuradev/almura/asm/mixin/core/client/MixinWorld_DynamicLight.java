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
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client side dynamic light for things drawn straight from the world's light (entities, tile entities, particles): include light from
 * players wearing a lighted helmet (see {@link ClientDynamicLightManager}). They pick it up instantly, without re-rendering anything. Only
 * client worlds are affected, and nothing is written to the world.
 */
@Mixin(World.class)
public abstract class MixinWorld_DynamicLight {

    @Shadow @Final public boolean isRemote;

    @Inject(method = "getCombinedLight", at = @At("RETURN"), cancellable = true)
    private void onGetCombinedLight(final BlockPos pos, final int lightValue, final CallbackInfoReturnable<Integer> cir) {
        if (this.isRemote) {
            cir.setReturnValue(ClientDynamicLightManager.withDynamicLight(cir.getReturnValueI(), pos));
        }
    }
}
