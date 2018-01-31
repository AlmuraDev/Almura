/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.normal;

import com.almuradev.content.type.block.component.aabb.BlockAABB;
import com.almuradev.content.type.block.type.normal.state.NormalBlockStateDefinition;
import net.minecraft.block.Block;
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

public final class NormalBlockImpl extends Block implements NormalBlock {
    private final NormalBlockStateDefinition definition;
    @Nullable private final BlockAABB.Box box;
    @Nullable private final BlockAABB.Collision collisionBox;
    @Nullable private final BlockAABB.WireFrame wireFrame;

    NormalBlockImpl(final NormalBlockBuilder builder) {
        this(builder, builder.singleState());
        builder.fill(this);
    }

    private NormalBlockImpl(final NormalBlockBuilder builder, final NormalBlockStateDefinition definition) {
        super((Material) builder.material.get());
        this.definition = definition;
        this.box = definition.box;
        this.collisionBox = definition.collisionBox;
        this.wireFrame = definition.wireFrame;
        definition.hardness.ifPresent(hardness -> this.setHardness((float) hardness));
        definition.lightEmission.ifPresent(emission -> this.setLightLevel((float) emission));
        definition.lightOpacity.ifPresent(this::setLightOpacity);
        definition.resistance.ifPresent(resistance -> this.setResistance((float) resistance));
    }

    @Override
    public NormalBlockStateDefinition definition(final IBlockState state) {
        return this.definition;
    }

    @Deprecated
    @Override
    @SuppressWarnings("ConstantConditions")
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return this.box != null ? this.box.box() : super.getBoundingBox(state, world, pos);
    }

    @Deprecated
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return this.collisionBox != null ? this.collisionBox.box() : super.getCollisionBoundingBox(state, world, pos);
    }

    @Deprecated
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public AxisAlignedBB getSelectedBoundingBox(final IBlockState state, final World world, final BlockPos pos) {
        return this.wireFrame != null ? this.wireFrame.box().offset(pos) : super.getSelectedBoundingBox(state, world, pos);
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
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }
}
