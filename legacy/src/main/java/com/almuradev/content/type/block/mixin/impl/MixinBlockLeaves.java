/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.mixin.impl;

import com.almuradev.content.type.block.mixin.iface.IMixinBlock;
import net.minecraft.block.BlockLeaves;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockLeaves.class, priority = 1001)
public abstract class MixinBlockLeaves {

    @Inject(method = "destroy", at = @At("HEAD"))
    private void setDropsToDecayOn(World worldIn, BlockPos pos, CallbackInfo ci) {
        ((IMixinBlock) this).setDropsFromDecay(true);
    }

    @Inject(method = "destroy", at = @At("RETURN"))
    private void setDropsToDecayOff(World worldIn, BlockPos pos, CallbackInfo ci) {
        ((IMixinBlock) this).setDropsFromDecay(false);
    }
}
