/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container.state;

import com.almuradev.content.type.block.BlockStateDefinition;

public final class ContainerBlockStateDefinitionBuilderImpl extends BlockStateDefinition.Builder.Impl<ContainerBlockStateDefinition, ContainerBlockStateDefinitionBuilderImpl> implements ContainerBlockStateDefinitionBuilder {

    @Override
    public ContainerBlockStateDefinition build0() {
        return new ContainerBlockStateDefinition(this);
    }
}
