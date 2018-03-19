/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.stair;

import com.almuradev.content.type.block.AbstractBlockBuilder;
import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.block.type.stair.state.StairBlockStateDefinition;
import com.almuradev.content.type.block.type.stair.state.StairBlockStateDefinitionBuilder;
import com.almuradev.content.type.block.type.stair.state.StairBlockStateDefinitionBuilderImpl;

public class StairBlockBuilder extends AbstractBlockBuilder<StairBlock, StairBlockStateDefinition, StairBlockStateDefinitionBuilder> implements StairBlock.Builder {
    public LazyBlockState block;

    public StairBlockBuilder() {
        super(BlockGenre.STAIR);
    }

    @Override
    protected StairBlockStateDefinitionBuilder createDefinitionBuilder(final String id) {
        return new StairBlockStateDefinitionBuilderImpl();
    }

    @Override
    public void block(final LazyBlockState block) {
        this.block = block;
    }

    @Override
    public StairBlock build() {
        return new StairBlockImpl(this);
    }
}
