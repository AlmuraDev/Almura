/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockWall.class)
public abstract class MixinBlockWall extends Block {

    protected MixinBlockWall(Material material) {
        super(material);
    }

    @Inject(method = "canConnectWallTo", at = @At("RETURN"), cancellable = true)
    public void onCanConnectWallTo(IBlockAccess access, int x, int y, int z, CallbackInfoReturnable<Boolean> ci) {
        final Block block = access.getBlock(x, y, z);
        if (block instanceof BlockWall) {
            ci.setReturnValue(true);
        }
    }
}
