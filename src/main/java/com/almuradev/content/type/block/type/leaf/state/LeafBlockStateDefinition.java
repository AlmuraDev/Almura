/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf.state;

import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.component.aabb.BlockAABB;

public final class LeafBlockStateDefinition extends BlockStateDefinition.Impl.Single<BlockAABB.Box, BlockAABB.Collision, BlockAABB.WireFrame> {
    LeafBlockStateDefinition(final LeafBlockStateDefinitionBuilderImpl builder) {
        super(builder);
    }
}
