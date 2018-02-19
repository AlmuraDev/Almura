/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling.state;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.AbstractSingleBlockStateDefinition;
import com.almuradev.content.type.block.component.aabb.BlockAABB;
import com.almuradev.content.type.tree.Tree;

public final class SaplingBlockStateDefinition extends AbstractSingleBlockStateDefinition<BlockAABB.Box, BlockAABB.Collision, BlockAABB.WireFrame> {
    public final Delegate<Tree> tree;

    SaplingBlockStateDefinition(final SaplingBlockStateDefinitionBuilderImpl builder) {
        super(builder);
        this.tree = builder.tree;
    }
}
