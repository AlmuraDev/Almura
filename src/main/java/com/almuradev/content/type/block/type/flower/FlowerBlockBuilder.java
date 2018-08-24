/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.flower;

import com.almuradev.content.type.block.AbstractBlockBuilder;
import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.type.flower.state.FlowerBlockStateDefinition;
import com.almuradev.content.type.block.type.flower.state.FlowerBlockStateDefinitionBuilder;
import com.almuradev.content.type.block.type.flower.state.FlowerBlockStateDefinitionBuilderImpl;

import java.util.List;
import java.util.stream.Collectors;

public final class FlowerBlockBuilder extends AbstractBlockBuilder<FlowerBlock, FlowerBlockStateDefinition, FlowerBlockStateDefinitionBuilder> implements FlowerBlock.Builder {
    public FlowerBlockBuilder() {
        super(BlockGenre.FLOWER);
    }

    @Override
    protected FlowerBlockStateDefinitionBuilder createDefinitionBuilder(final String id) {
        return new FlowerBlockStateDefinitionBuilderImpl();
    }

    @Override
    public FlowerBlock build() {
        final List<FlowerBlockStateDefinition> states = this.stateBuilders().values().stream()
                .map(FlowerBlockStateDefinitionBuilder::build)
                .collect(Collectors.toList());
        return new FlowerBlockImpl(this, states);
    }
}
