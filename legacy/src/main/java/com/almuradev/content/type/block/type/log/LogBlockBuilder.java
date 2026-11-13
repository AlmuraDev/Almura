/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.log;

import com.almuradev.content.type.block.AbstractBlockBuilder;
import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.type.log.state.LogBlockStateDefinition;
import com.almuradev.content.type.block.type.log.state.LogBlockStateDefinitionBuilder;
import com.almuradev.content.type.block.type.log.state.LogBlockStateDefinitionBuilderImpl;

public final class LogBlockBuilder extends AbstractBlockBuilder<LogBlock, LogBlockStateDefinition, LogBlockStateDefinitionBuilder> implements LogBlock.Builder {
    public LogBlockBuilder() {
        super(BlockGenre.LOG);
    }

    @Override
    protected LogBlockStateDefinitionBuilder createDefinitionBuilder(final String id) {
        return new LogBlockStateDefinitionBuilderImpl();
    }

    @Override
    public LogBlock build() {
        return new LogBlockImpl(this);
    }
}
