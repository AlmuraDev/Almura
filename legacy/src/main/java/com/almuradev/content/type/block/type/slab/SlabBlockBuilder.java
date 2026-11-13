/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.slab;

import com.almuradev.content.type.block.AbstractBlockBuilder;
import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.block.type.slab.state.SlabBlockStateDefinition;
import com.almuradev.content.type.block.type.slab.state.SlabBlockStateDefinitionBuilder;
import com.almuradev.content.type.block.type.slab.state.SlabBlockStateDefinitionBuilderImpl;

public final class SlabBlockBuilder extends AbstractBlockBuilder<SlabBlock, SlabBlockStateDefinition, SlabBlockStateDefinitionBuilder> implements SlabBlock.Builder {
    public boolean isDouble;
    public LazyBlockState single;

    public SlabBlockBuilder() {
        super(BlockGenre.SLAB);
    }

    @Override
    protected SlabBlockStateDefinitionBuilder createDefinitionBuilder(final String id) {
        return new SlabBlockStateDefinitionBuilderImpl();
    }

    @Override
    public void isDouble(final boolean isDouble) {
        this.isDouble = isDouble;
    }

    @Override
    public void single(final LazyBlockState single) {
        this.single = single;
    }

    @Override
    public SlabBlock build() {
        return new SlabBlockImpl(this);
    }
}
