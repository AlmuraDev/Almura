/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.normal;

import com.almuradev.content.type.block.AbstractBlockBuilder;
import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.type.normal.state.NormalBlockStateDefinition;
import com.almuradev.content.type.block.type.normal.state.NormalBlockStateDefinitionBuilder;
import com.almuradev.content.type.block.type.normal.state.NormalBlockStateDefinitionBuilderImpl;

public final class NormalBlockBuilder extends AbstractBlockBuilder<NormalBlock, NormalBlockStateDefinition, NormalBlockStateDefinitionBuilder> implements NormalBlock.Builder {
    public NormalBlockBuilder() {
        super(BlockGenre.NORMAL);
    }

    @Override
    protected NormalBlockStateDefinitionBuilder createDefinitionBuilder(final String id) {
        return new NormalBlockStateDefinitionBuilderImpl();
    }

    @Override
    public NormalBlock build() {
        return new NormalBlockImpl(this);
    }
}
