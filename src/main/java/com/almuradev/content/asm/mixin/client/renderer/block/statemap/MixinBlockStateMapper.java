/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.asm.mixin.client.renderer.block.statemap;

import com.almuradev.content.type.block.SpecialBlockStateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BlockStateMapper.class)
@SideOnly(Side.CLIENT)
public class MixinBlockStateMapper {

    @Inject(
            method = "getVariants",
            at = @At("HEAD"),
            cancellable = true
    )
    private void provideSpecialBlockStateVariants(final Block block, final CallbackInfoReturnable<Map<IBlockState, ModelResourceLocation>> cir) {
        if (block instanceof SpecialBlockStateBlock) {
            cir.setReturnValue(com.almuradev.content.type.block.BlockStateMapper.INSTANCE.putStateModelLocations(block));
        }
    }
}
