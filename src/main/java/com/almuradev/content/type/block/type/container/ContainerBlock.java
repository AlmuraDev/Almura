/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container;

import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.content.type.block.type.container.state.ContainerBlockStateDefinition;
import com.almuradev.content.type.block.type.container.state.ContainerBlockStateDefinitionBuilder;

public interface ContainerBlock extends ContentBlockType.InInventory {

    interface Builder extends ContentBlockType.Builder.Single<ContainerBlock, ContainerBlockStateDefinition, ContainerBlockStateDefinitionBuilder> {

        void limit(final int limit);
    }
}
