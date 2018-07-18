/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container;

import com.almuradev.content.type.block.AbstractBlockBuilder;
import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.type.container.state.ContainerBlockStateDefinition;
import com.almuradev.content.type.block.type.container.state.ContainerBlockStateDefinitionBuilder;
import com.almuradev.content.type.block.type.container.state.ContainerBlockStateDefinitionBuilderImpl;

public final class ContainerBlockBuilder extends AbstractBlockBuilder<ContainerBlock, ContainerBlockStateDefinition, ContainerBlockStateDefinitionBuilder> implements ContainerBlock.Builder {
    int limit;

    public ContainerBlockBuilder() {
        super(BlockGenre.CONTAINER);
    }

    @Override
    protected ContainerBlockStateDefinitionBuilder createDefinitionBuilder(final String id) {
        return new ContainerBlockStateDefinitionBuilderImpl();
    }

    @Override
    public ContainerBlock build() {
        return new ContainerBlockImpl(this);
    }

    @Override
    public void limit(final int limit) {
        this.limit = limit;
    }
}
