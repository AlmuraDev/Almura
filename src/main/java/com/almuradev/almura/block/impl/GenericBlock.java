/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.impl;

import com.almuradev.almura.block.BuildableBlockType;
import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public final class GenericBlock extends Block {

    public GenericBlock(BuildableBlockType.Builder<?, ?> builder) {
        super(builder.material().orElse(null), builder.mapColor().orElse(null));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", ((BuildableBlockType) (Object) this).getId())
                .add("unlocalizedName", this.getUnlocalizedName())
                .add("creativeTab", ((BuildableBlockType) (Object) this).getCreativeTab().orElse(null))
                .add("material", this.blockMaterial)
                .add("mapColor", this.blockMapColor)
                .add("hardness", this.blockHardness)
                .add("resistance", this.blockResistance)
                .toString();
    }
}
