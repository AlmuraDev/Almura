/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf;

import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinition;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.List;

import javax.annotation.Nonnull;

public final class LeafBlockImpl extends BlockLeaves implements LeafBlock {
    private final LeafBlockStateDefinition definition;

    LeafBlockImpl(final LeafBlockBuilder builder) {
        builder.fill(this);
        this.definition = builder.singleState();
        this.definition.fill(this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE);
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
}
