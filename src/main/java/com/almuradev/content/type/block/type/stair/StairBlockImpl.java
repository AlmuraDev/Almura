/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.stair;

import com.almuradev.almura.asm.mixin.accessors.block.BlockAccessor;
import com.almuradev.almura.asm.mixin.accessors.block.BlockStairsAccessor;
import com.almuradev.content.component.delegate.DelegateNotSatisfiedException;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.block.type.stair.state.StairBlockStateDefinition;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import javax.annotation.Nullable;

public final class StairBlockImpl extends BlockStairs implements StairBlock {
    private final StairBlockStateDefinition definition;
    private final LazyBlockState block;
    private boolean baseResolved;

    StairBlockImpl(final StairBlockBuilder builder) {
        // Content blocks are all built before any is registered, so a stair made from another content block (Almura's planks) can't find
        // it yet: stand in with oak planks and take the real block once it's registered (resolveBase)
        super(baseOrStandIn(builder.block));
        ((BlockAccessor) (Object) this).accessor$setDisplayOnCreativeTab(null);
        builder.fill(this);
        this.definition = builder.singleState();
        this.definition.fill(this);
        this.useNeighborBrightness = true;
        this.block = builder.block;
        this.baseResolved = baseOrNull(this.block) != null;
    }

    private static IBlockState baseOrStandIn(final LazyBlockState block) {
        @Nullable final IBlockState base = baseOrNull(block);
        return base != null ? base : Blocks.PLANKS.getDefaultState();
    }

    @Nullable
    private static IBlockState baseOrNull(final LazyBlockState block) {
        try {
            return block.get();
        } catch (final DelegateNotSatisfiedException e) {
            return null;
        }
    }

    /**
     * Points the stair at its real base block, for stairs whose base wasn't registered yet when they were built. Called once every content
     * block is registered.
     */
    public void resolveBase() {
        if (this.baseResolved) {
            return;
        }
        final IBlockState base = this.block.get();
        ((BlockStairsAccessor) (Object) this).accessor$setModelBlock(base.getBlock());
        ((BlockStairsAccessor) (Object) this).accessor$setModelState(base);
        this.baseResolved = true;
    }

    @Override
    public StairBlockStateDefinition definition(final IBlockState state) {
        return this.definition;
    }
}
