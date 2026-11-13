/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal;

import com.almuradev.content.type.block.PlacementRotation;
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
import net.minecraft.util.math.Vec3d;
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
    // Light is asked for constantly while chunks are lit and rendered, so each state's light value and flicker flag are worked out
    // once here. Indexed together; a light value of -1 means "use the block's own light value". Null until the constructor finishes.
    @Nullable private IBlockState[] lightStates;
    @Nullable private int[] lightValues;
    @Nullable private boolean[] flickerStates;

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

        this.cacheLight();
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

    private void cacheLight() {
        final List<IBlockState> validStates = this.getBlockState().getValidStates();
        final IBlockState[] lightStates = new IBlockState[validStates.size()];
        final int[] lightValues = new int[lightStates.length];
        final boolean[] flickerStates = new boolean[lightStates.length];
        for (int i = 0; i < lightStates.length; i++) {
            final IBlockState state = validStates.get(i);
            final HorizontalBlockStateDefinition definition = this.definition(state);
            lightStates[i] = state;
            lightValues[i] = definition == null ? -1 : PrimitiveOptionals.mapToInt(definition.lightEmission, value -> (int) (15f * value)).orElse(-1);
            flickerStates[i] = definition != null && definition.flicker;
        }
        this.lightValues = lightValues;
        this.flickerStates = flickerStates;
        this.lightStates = lightStates; // Last, as it's what marks the cache as ready
    }

    /**
     * @return the index of the state in the light cache, or -1 when the cache isn't ready or doesn't have it
     */
    private int lightIndex(final IBlockState state) {
        final IBlockState[] lightStates = this.lightStates;
        if (lightStates != null) {
            for (int i = 0; i < lightStates.length; i++) {
                if (lightStates[i] == state) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Deprecated
    @Override
    public int getLightValue(final IBlockState state) {
        final int index = this.lightIndex(state);
        if (index >= 0) {
            final int lightValue = this.lightValues[index];
            return lightValue >= 0 ? lightValue : super.getLightValue(state);
        }
        if(state != null && this.states != null) {
            final HorizontalBlockStateDefinition definition = this.definition(state);
            return PrimitiveOptionals.mapToInt(definition.lightEmission, value -> (int) (15f * value)).orElseGet(() -> super.getLightValue(state));
        }
        return 0;
    }

    @Override
    public int getLightValue(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        if (state == null) {
            return 0;
        }
        final int index = this.lightIndex(state);
        final int cached = index >= 0 ? this.lightValues[index] : -1;
        final int lightValue = cached >= 0 ? cached : this.getLightValue(state);
        // Flickering lights only dim on the client; the server always gets the steady light
        if (index >= 0 ? this.flickerStates[index] : this.isFlicker(state)) {
            return FlickerLights.getLightValue(world, pos, lightValue);
        }
        return lightValue;
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
        // Pipes placed against the side of a block (such as the end of another pipe) turn to connect to it: a straight pipe, whose
        // model runs across the facing, continues out from that block; a T pipe points its branch into it; a corner puts one arm
        // into it. Placed on a floor or ceiling, they keep the usual facing.
        final PlacementRotation rotation = this.definition(EnumFacing.NORTH).rotatePlacement;
        if (facing.getAxis().isHorizontal() && rotation != null) {
            switch (rotation) {
                case ACROSS:
                    placed = facing.rotateY();
                    break;
                case TOWARD:
                    placed = facing.getOpposite();
                    break;
                case CORNER:
                    placed = cornerFacing(facing, pos, placer);
                    break;
            }
        }
        return this.getDefaultState().withProperty(FACING, placed);
    }

    /**
     * The facing of a corner (arms west and south when facing north) placed against a block's side: one arm into that block, the
     * other toward the side the player stands on, or looks toward when standing in line with it.
     */
    private static EnumFacing cornerFacing(final EnumFacing clickedFace, final BlockPos pos, final EntityLivingBase placer) {
        final EnumFacing right = clickedFace.getOpposite().rotateY(); // right of the arm into the clicked block
        double side = (placer.posX - (pos.getX() + 0.5)) * right.getXOffset() + (placer.posZ - (pos.getZ() + 0.5)) * right.getZOffset();
        if (Math.abs(side) < 0.5) {
            final Vec3d look = placer.getLookVec();
            side = look.x * right.getXOffset() + look.z * right.getZOffset();
        }
        // facing the clicked face, the arms point into the clicked block and to its right; facing right, into it and to its left
        return side >= 0 ? clickedFace : right;
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
        final int index = this.lightIndex(state);
        if (index >= 0) {
            return this.flickerStates[index];
        }
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
