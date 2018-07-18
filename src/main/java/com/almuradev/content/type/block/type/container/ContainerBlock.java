/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container;

import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.type.container.state.ContainerBlockStateDefinition;
import com.almuradev.content.type.block.type.container.state.ContainerBlockStateDefinitionBuilder;
import net.minecraft.block.state.IBlockState;

public interface ContainerBlock extends ContentBlock.InInventory {
    @Override
    ContainerBlockStateDefinition definition(final IBlockState state);

    interface Builder extends ContentBlock.Builder.Single<ContainerBlock, ContainerBlockStateDefinition, ContainerBlockStateDefinitionBuilder> {
        void limit(final int limit);
    }
}
