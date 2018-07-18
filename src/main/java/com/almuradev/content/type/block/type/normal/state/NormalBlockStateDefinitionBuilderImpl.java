/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.normal.state;

import com.almuradev.content.type.block.AbstractBlockStateDefinitionBuilder;

public final class NormalBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<NormalBlockStateDefinition, NormalBlockStateDefinitionBuilderImpl> implements NormalBlockStateDefinitionBuilder {
    @Override
    public NormalBlockStateDefinition build0() {
        return new NormalBlockStateDefinition(this);
    }
}
