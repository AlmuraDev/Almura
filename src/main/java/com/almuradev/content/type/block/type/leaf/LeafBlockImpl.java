/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf;

import com.almuradev.almura.asm.mixin.accessors.block.BlockAccessor;
import com.almuradev.almura.asm.mixin.accessors.block.BlockLeavesAccessor;
import com.almuradev.content.type.block.StateMappedBlock;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinition;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import javax.annotation.Nonnull;

public final class LeafBlockImpl extends BlockLeaves implements LeafBlock, StateMappedBlock {
    @Deprecated private static final int LEGACY_DECAYABLE = 1;
    @Deprecated private static final int LEGACY_CHECK_DECAY = 2;
    private final LeafBlockStateDefinition definition;

    LeafBlockImpl(final LeafBlockBuilder builder) {
        ((BlockAccessor) (Object) this).accessor$setDisplayOnCreativeTab(null);
        builder.fill(this);
        this.definition = builder.singleState();
        this.definition.fill(this);
        this.setDefaultState(
                this.blockState.getBaseState()
                        .withProperty(CHECK_DECAY, true)
                        .withProperty(DECAYABLE, true)
        );
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE);
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(final int data) {
        return this.getDefaultState()
                .withProperty(DECAYABLE, (data & LEGACY_DECAYABLE) > 0)
                .withProperty(CHECK_DECAY, (data & LEGACY_CHECK_DECAY) > 0);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return !((BlockLeavesAccessor) (Object) this).accessor$getIsLeavesFancy();
    }

    @Deprecated
    @Override
    public int getMetaFromState(final IBlockState state) {
        int data = 0;
        if (state.getValue(DECAYABLE)) {
            data |= LEGACY_DECAYABLE;
        }
        if (state.getValue(CHECK_DECAY)) {
            data |= LEGACY_CHECK_DECAY;
        }
        return data;
    }

    @Override
    public LeafBlockStateDefinition definition(final IBlockState state) {
        return this.definition;
    }

    @Override
    public BlockPlanks.EnumType getWoodType(final int meta) {
        return BlockPlanks.EnumType.DARK_OAK; // I'm not even related to dark_oak, but this goes away with 1.13
    }

    @Override
    public List<ItemStack> onSheared(@Nonnull final ItemStack item, final IBlockAccess world, final BlockPos pos, final int fortune) {
        return NonNullList.withSize(1, new ItemStack(this));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IStateMapper createStateMapper() {
        return new StateMap.Builder()
                .ignore(CHECK_DECAY, DECAYABLE)
                .build();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return (((BlockLeavesAccessor) (Object) this).accessor$getIsLeavesFancy() || blockAccess.getBlockState(pos.offset(side)).getBlock() != this) && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
}
