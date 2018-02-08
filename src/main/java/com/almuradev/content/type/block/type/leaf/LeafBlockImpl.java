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
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
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
    public LeafBlockStateDefinition definition(final IBlockState state) {
        return this.definition;
    }

    @Override
    public BlockPlanks.EnumType getWoodType(final int meta) {
        return null; // TODO
    }

    @Override
    public List<ItemStack> onSheared(@Nonnull final ItemStack item, final IBlockAccess world, final BlockPos pos, final int fortune) {
        return null; // TODO
    }
}
