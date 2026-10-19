/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal;

import com.almuradev.content.type.block.flicker.FlickerLightTileEntity;
import com.almuradev.content.type.block.flicker.FlickerLights;
import com.almuradev.content.type.block.type.horizontal.state.HorizontalBlockStateDefinition;
import net.kyori.lunar.PrimitiveOptionals;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
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
import java.util.Random;

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
            final int lightValue = this.getLightValue(state);
            // Flickering lights only dim on the client; the server always gets the steady light
            if (this.isFlicker(state)) {
                return FlickerLights.getLightValue(world, pos, lightValue);
            }
            return lightValue;
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
        EnumFacing placed = placer.getHorizontalFacing().getOpposite();
        // e.g. straight pipes, whose model runs across the facing: placed against the side of a block (such as the end of another pipe),
        // turn them so they continue out from that block. Placed on a floor or ceiling, they keep the usual facing.
        if (facing.getAxis().isHorizontal() && this.definition(EnumFacing.NORTH).rotatePlacement) {
            placed = facing.rotateY();
        }
        return this.getDefaultState().withProperty(FACING, placed);
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

    private boolean isFlicker(final IBlockState state) {
        // Can be asked before the states are filled in during construction
        if (this.states == null) {
            return false;
        }
        final HorizontalBlockStateDefinition definition = this.definition(state);
        return definition != null && definition.flicker;
    }
    @Override
    public boolean hasTileEntity(final IBlockState state) {
        return this.isFlicker(state);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return this.isFlicker(state) ? new FlickerLightTileEntity() : null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(final IBlockState state, final World world, final BlockPos pos, final Random random) {
        final EnumFacing facing = state.getValue(FACING);
        final HorizontalBlockStateDefinition definition = this.definition(facing);
        // Same odds as water dripping through the block below it
        if (!definition.drip || random.nextInt(10) != 0) {
            return;
        }

        // Drip from the underside of the block's shape, near its middle where the pipe runs
        final AxisAlignedBB box = definition.box(facing);
        final double bottom = box != null ? box.minY : 0;
        final double x = pos.getX() + 0.35 + random.nextDouble() * 0.3;
        final double y = pos.getY() + bottom - 0.05;
        final double z = pos.getZ() + 0.35 + random.nextDouble() * 0.3;
        world.spawnParticle(EnumParticleTypes.DRIP_WATER, x, y, z, 0, 0, 0);
    }
}
