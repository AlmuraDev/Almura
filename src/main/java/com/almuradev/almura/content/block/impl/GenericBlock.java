/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.impl;

import com.almuradev.almura.asm.mixin.interfaces.IMixinAlmuraBlock;
import com.almuradev.almura.content.block.BuildableBlockType;
import com.almuradev.almura.content.block.component.aabb.CollisionBox;
import com.almuradev.almura.content.block.component.aabb.WireFrame;
import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public final class GenericBlock extends Block {

    private final CollisionBox collisionAABB;
    private final WireFrame wireFrameAABB;

    public GenericBlock(BuildableBlockType.Builder<?, ?> builder) {
        super((Material) builder.material(), (MapColor) builder.mapColor());
        this.collisionAABB = builder.collisionAABB();
        this.wireFrameAABB = builder.wireFrameAABB();
        ((IMixinAlmuraBlock) (Object) this).setBreaks(builder.breaks());
    }

    @Deprecated
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return this.collisionAABB.provided ? this.collisionAABB.bb : super.getCollisionBoundingBox(state, world, pos);
    }

    @Deprecated
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        return this.wireFrameAABB.bb != null ? this.wireFrameAABB.bb.offset(pos) : super.getSelectedBoundingBox(state, world, pos);
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
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", ((BuildableBlockType) (Object) this).getId())
                .add("unlocalizedName", this.getUnlocalizedName())
                .add("itemGroup", ((BuildableBlockType) (Object) this).getItemGroup().orElse(null))
                .add("material", this.blockMaterial)
                .add("mapColor", this.blockMapColor)
                .add("hardness", this.blockHardness)
                .add("resistance", this.blockResistance)
                .toString();
    }
}
