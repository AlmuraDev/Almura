/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.normal;

import com.almuradev.content.type.block.type.normal.state.NormalBlockStateDefinition;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class NormalBlockImpl extends Block implements NormalBlock {
    private final NormalBlockStateDefinition definition;

    NormalBlockImpl(final NormalBlockBuilder builder) {
        super((Material) builder.material.get());
        builder.fill(this);
        this.definition = builder.singleState();
        this.definition.fill(this);
    }

    @Override
    public NormalBlockStateDefinition definition(final IBlockState state) {
        return this.definition;
    }

    @Deprecated
    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return true;
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }
}
