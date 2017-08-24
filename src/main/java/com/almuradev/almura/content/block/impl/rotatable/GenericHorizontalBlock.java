/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.impl.rotatable;

import com.almuradev.almura.asm.mixin.interfaces.IMixinAlmuraBlock;
import com.almuradev.almura.content.block.builder.rotatable.HorizontalTypeBuilderImpl;
import com.almuradev.almura.content.block.impl.BlockAABB;
import com.almuradev.almura.content.block.rotatable.HorizontalType;
import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public final class GenericHorizontalBlock extends BlockHorizontal {

    private final BlockAABB.Collision collisionAABB;
    private final BlockAABB.WireFrame wireFrameAABB;

    public GenericHorizontalBlock(HorizontalTypeBuilderImpl builder) {
        super((Material) builder.material(), (MapColor) builder.mapColor());
        this.collisionAABB = builder.collisionAABB();
        this.wireFrameAABB = builder.wireFrameAABB();
        ((IMixinAlmuraBlock) (Object) this).setBreaks(builder.breaks());
    }

    @Deprecated
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return this.collisionAABB.provided ? this.collisionAABB.horizontal()[state.getValue(FACING).getHorizontalIndex()] : super.getCollisionBoundingBox(state, world, pos);
    }

    @Deprecated
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        return this.wireFrameAABB.bb != null ? this.wireFrameAABB.horizontal()[state.getValue(FACING).getHorizontalIndex()].offset(pos) : super.getSelectedBoundingBox(state, world, pos);
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Deprecated
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Deprecated
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Deprecated
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
                .add("itemGroup", ((HorizontalType) (Object) this).getItemGroup().orElse(null))
                .add("material", this.blockMaterial)
                .add("mapColor", this.blockMapColor)
                .add("hardness", this.blockHardness)
                .add("resistance", this.blockResistance)
                .toString();
    }
}
