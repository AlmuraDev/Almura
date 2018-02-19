/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling;

import com.almuradev.content.type.block.AbstractBlockBuilder;
import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.type.sapling.state.SaplingBlockStateDefinition;
import com.almuradev.content.type.block.type.sapling.state.SaplingBlockStateDefinitionBuilder;
import com.almuradev.content.type.block.type.sapling.state.SaplingBlockStateDefinitionBuilderImpl;

public final class SaplingBlockBuilder extends AbstractBlockBuilder<SaplingBlock, SaplingBlockStateDefinition, SaplingBlockStateDefinitionBuilder> implements SaplingBlock.Builder {
    public SaplingBlockBuilder() {
        super(BlockGenre.SAPLING);
    }

    @Override
    protected SaplingBlockStateDefinitionBuilder createDefinitionBuilder(final String id) {
        return new SaplingBlockStateDefinitionBuilderImpl();
    }

    @Override
    public SaplingBlock build() {
        return new SaplingBlockImpl(this);
    }
}
