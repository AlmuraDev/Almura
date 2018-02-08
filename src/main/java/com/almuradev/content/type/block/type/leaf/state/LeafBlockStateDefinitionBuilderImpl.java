/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf.state;

import com.almuradev.content.type.block.BlockStateDefinition;

public final class LeafBlockStateDefinitionBuilderImpl extends BlockStateDefinition.Builder.Impl<LeafBlockStateDefinition, LeafBlockStateDefinitionBuilderImpl> implements LeafBlockStateDefinitionBuilder {
    @Override
    public LeafBlockStateDefinition build0() {
        return new LeafBlockStateDefinition(this);
    }
}
