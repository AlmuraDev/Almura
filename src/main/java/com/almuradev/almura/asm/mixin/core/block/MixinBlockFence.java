/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BlockFence.class, priority = 999)
public abstract class MixinBlockFence extends Block {

    @Shadow public static boolean isExcepBlockForAttachWithPiston(Block block) { return false; }

    public MixinBlockFence(Material materialIn, MapColor mapColorIn) {
        super(materialIn, mapColorIn);
    }

    /**
     * @author Dockter - Mike Howe
     * @reason Hack to prevent fences from trying to connect to Custom Lights.
     */
    @Overwrite
    public boolean canConnectTo(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        BlockFaceShape blockfaceshape = iblockstate.getBlockFaceShape(worldIn, pos, facing);
        Block block = iblockstate.getBlock();
        if (block.getUnlocalizedName().equalsIgnoreCase("tile.almura.horizontal.lighting.almuracustom1_light") || block.getUnlocalizedName().equalsIgnoreCase("tile.almura.horizontal.lighting.almuracustom2_offlight"))
            return false;

        boolean flag = blockfaceshape == BlockFaceShape.MIDDLE_POLE && (iblockstate.getMaterial() == this.blockMaterial || block instanceof BlockFenceGate);
        return !isExcepBlockForAttachWithPiston(block) && blockfaceshape == BlockFaceShape.SOLID || flag;
    }

}
