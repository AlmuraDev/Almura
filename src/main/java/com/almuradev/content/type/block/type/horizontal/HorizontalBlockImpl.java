/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal;

import com.almuradev.content.type.block.type.horizontal.state.HorizontalBlockStateDefinition;
import net.kyori.lunar.PrimitiveOptionals;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public final class HorizontalBlockImpl extends BlockHorizontal implements HorizontalBlock {
    private final Map<EnumFacing, HorizontalBlockStateDefinition> states = new EnumMap<>(EnumFacing.class);

    HorizontalBlockImpl(final HorizontalBlockBuilder builder, final List<HorizontalBlockStateDefinition> states) {
        super((Material) builder.material.get());
        builder.fill(this);

        // Fix Traits
        this.lightOpacity = 0;  // 0 = will not decrease neighbor or skylight values, 255, means absolute darkness, act as a full block essentially to block light.
        this.translucent = false;
        this.useNeighborBrightness = false;
        this.fullBlock = false;

        // assume first state is the default
        final Boolean opaque = states.get(0).opaque;
        if(opaque != null) {
            this.fullBlock = opaque;
        }

        for (final HorizontalBlockStateDefinition state : states) {
            this.states.put(state.facing, state);
        }
    }

    @Override
    public HorizontalBlockStateDefinition definition(final IBlockState state) {
        return this.definition(state.getValue(FACING));
    }

    private HorizontalBlockStateDefinition definition(final EnumFacing facing) {
        return this.states.get(facing);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Deprecated
    @Override
    @SuppressWarnings("ConstantConditions")
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final EnumFacing facing = state.getValue(FACING);
        final AxisAlignedBB box = this.definition(facing).box(facing);
        return box != null ? box : super.getBoundingBox(state, world, pos);
    }

    @Deprecated
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final EnumFacing facing = state.getValue(FACING);
        final HorizontalBlockStateDefinition definition = this.definition(facing);
        if (definition.nullCollisionBox) {
            return null;
        }
        final AxisAlignedBB collisionBox = definition.collisionBox(facing);
        return collisionBox != null ? collisionBox : super.getCollisionBoundingBox(state, world, pos);
    }

    @Deprecated
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(final IBlockState state, final World world, final BlockPos pos) {
        final EnumFacing facing = state.getValue(FACING);
        final AxisAlignedBB wireFrame = this.definition(facing).wireFrame(facing, pos);
        return wireFrame != null ? wireFrame : super.getSelectedBoundingBox(state, world, pos);
    }

    @Deprecated
    @Override
    public float getBlockHardness(final IBlockState state, final World world, final BlockPos pos) {
        final HorizontalBlockStateDefinition definition = this.definition(state);
        return definition.hardness.isPresent() ? (float) definition.hardness.getAsDouble() : super.getBlockHardness(state, world, pos);
    }

    @Deprecated
    @Override
    public int getLightOpacity(final IBlockState state) {
        final HorizontalBlockStateDefinition definition = this.definition(state);
        int lightOpacity = definition.lightOpacity.orElseGet(() -> super.getLightOpacity(state));
        //System.out.println("getLightOpacity = " + lightOpacity);
        return lightOpacity;
    }

    @Deprecated
    @Override
    public int getLightValue(final IBlockState state) {
        if(state != null && this.states != null) {
            final HorizontalBlockStateDefinition definition = this.definition(state);
            int lightValue = PrimitiveOptionals.mapToInt(definition.lightEmission, value -> (int) (15f * value)).orElseGet(() -> super.getLightValue(state));
            //System.out.println("getLightValue (state) = " + lightValue);
            return lightValue;
        } else {
            //System.out.println("getLightValue (fallback) = 0");
            return 0;
        }
    }

    @Override
    public int getLightValue(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        if (state != null) {
            return this.getLightValue(state);
        } else {
            //System.out.println("getLightValue Fallback2 = 0");
            return 0;
        }
    }

    @Deprecated
    @Override
    public float getExplosionResistance(final Entity exploder) {
        return (float) this.states.get(EnumFacing.NORTH).resistance.orElse(0);
    }

    @Override
    public float getExplosionResistance(final World world, final BlockPos pos, @Nullable final Entity exploder, final Explosion explosion) {
        final IBlockState state = world.getBlockState(pos);
        final HorizontalBlockStateDefinition definition = this.definition(state);
        return definition.resistance.isPresent() ? (float) definition.resistance.getAsDouble() : super.getExplosionResistance(exploder);
    }

    @Deprecated
    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }

    @Deprecated
    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean isOpaqueCube(final IBlockState state) {
        if(this.states != null) {
            final Boolean opaque = this.definition(state).opaque;
            if(opaque != null) {
                //System.out.println("isOpaqueCube 1 = " + opaque);
                return opaque;
            }
        }
        //System.out.println("isOpaqueCube 2 = false");
        return false;
    }

    @Deprecated
    @Override
    public IBlockState getStateForPlacement(final World world, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Deprecated
    @Override
    public boolean isTopSolid(IBlockState state) {
        return true;
    }
}
