/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf.state;

import com.almuradev.content.type.block.AbstractBlockStateDefinitionBuilder;

public final class LeafBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<LeafBlockStateDefinition, LeafBlockStateDefinitionBuilderImpl> implements LeafBlockStateDefinitionBuilder {
    @Override
    public LeafBlockStateDefinition build0() {
        return new LeafBlockStateDefinition(this);
    }
}
