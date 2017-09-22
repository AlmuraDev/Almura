/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.state.factory;

import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilder;

public abstract class BlockStateDefinitionBuilderFactory extends AbstractBlockStateDefinitionBuilderFactory<BlockStateDefinitionBuilder<?>> {

    @Override
    protected BlockStateDefinitionBuilder<?> createDefinitionBuilder() {
        return BlockStateDefinitionBuilder.normal();
    }
}
