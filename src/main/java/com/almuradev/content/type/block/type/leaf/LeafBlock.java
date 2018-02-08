/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf;

import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinition;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinitionBuilder;
import net.minecraft.block.state.IBlockState;

public interface LeafBlock extends ContentBlockType.InInventory {
    @Override
    LeafBlockStateDefinition definition(final IBlockState state);

    interface Builder extends ContentBlockType.Builder.Single<LeafBlock, LeafBlockStateDefinition, LeafBlockStateDefinitionBuilder> {
    }
}
