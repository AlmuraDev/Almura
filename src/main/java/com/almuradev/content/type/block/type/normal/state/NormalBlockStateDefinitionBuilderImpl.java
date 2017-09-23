/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.normal.state;

import com.almuradev.content.type.block.BlockStateDefinition;

public final class NormalBlockStateDefinitionBuilderImpl extends BlockStateDefinition.Builder.Impl<NormalBlockStateDefinition> implements NormalBlockStateDefinitionBuilder {

    @Override
    public NormalBlockStateDefinition build() {
        return new NormalBlockStateDefinition(this);
    }
}
