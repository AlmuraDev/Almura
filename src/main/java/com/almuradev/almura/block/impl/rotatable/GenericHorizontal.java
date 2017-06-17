/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.impl.rotatable;

import com.almuradev.almura.block.builder.rotatable.HorizontalTypeBuilderImpl;
import com.almuradev.almura.block.rotatable.HorizontalType;
import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class GenericHorizontal extends BlockHorizontal {

    public GenericHorizontal(HorizontalTypeBuilderImpl builder) {
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
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", ((HorizontalType) (Object) this).getId())
                .add("unlocalizedName", this.getUnlocalizedName())
                .add("creativeTab", ((HorizontalType) (Object) this).getCreativeTab().orElse(null))
                .add("material", this.blockMaterial)
                .add("mapColor", this.blockMapColor)
                .add("hardness", this.blockHardness)
                .add("resistance", this.blockResistance)
                .toString();
    }
}
