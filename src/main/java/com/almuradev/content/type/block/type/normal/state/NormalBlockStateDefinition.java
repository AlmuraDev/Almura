/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.normal.state;

import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.component.aabb.BlockAABB;

public final class NormalBlockStateDefinition extends BlockStateDefinition.Impl.Single<BlockAABB.Box, BlockAABB.Collision, BlockAABB.WireFrame> {

    NormalBlockStateDefinition(final NormalBlockStateDefinitionBuilderImpl builder) {
        super(builder);
    }
}
