/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf;

import com.almuradev.content.type.block.AbstractBlockBuilder;
import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinition;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinitionBuilder;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinitionBuilderImpl;

public final class LeafBlockBuilder extends AbstractBlockBuilder<LeafBlock, LeafBlockStateDefinition, LeafBlockStateDefinitionBuilder> implements LeafBlock.Builder {
    public LeafBlockBuilder() {
        super(BlockGenre.LEAF);
    }

    @Override
    protected LeafBlockStateDefinitionBuilder createDefinitionBuilder(final String id) {
        return new LeafBlockStateDefinitionBuilderImpl();
    }

    @Override
    public LeafBlock build() {
        return new LeafBlockImpl(this);
    }
}
