/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.log.state;

import com.almuradev.content.type.block.AbstractBlockStateDefinitionBuilder;

public final class LogBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<LogBlockStateDefinition, LogBlockStateDefinitionBuilderImpl> implements LogBlockStateDefinitionBuilder {
    @Override
    public LogBlockStateDefinition build0() {
        return new LogBlockStateDefinition(this);
    }
}
