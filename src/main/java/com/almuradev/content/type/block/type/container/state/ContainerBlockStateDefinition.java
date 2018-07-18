/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container.state;

import com.almuradev.content.type.block.AbstractSingleBlockStateDefinition;
import com.almuradev.content.type.block.component.aabb.BlockAABB;

public final class ContainerBlockStateDefinition extends AbstractSingleBlockStateDefinition<BlockAABB.Box, BlockAABB.Collision, BlockAABB.WireFrame> {
    ContainerBlockStateDefinition(final ContainerBlockStateDefinitionBuilderImpl builder) {
        super(builder);
    }
}
