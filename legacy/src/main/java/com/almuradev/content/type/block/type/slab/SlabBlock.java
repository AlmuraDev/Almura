/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.slab;

import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.block.type.slab.state.SlabBlockStateDefinition;
import com.almuradev.content.type.block.type.slab.state.SlabBlockStateDefinitionBuilder;
import net.minecraft.block.state.IBlockState;

public interface SlabBlock extends ContentBlock {
    @Override
    SlabBlockStateDefinition definition(final IBlockState state);

    interface Builder extends ContentBlock.Builder.Single<SlabBlock, SlabBlockStateDefinition, SlabBlockStateDefinitionBuilder> {
        void isDouble(final boolean isDouble);

        void single(final LazyBlockState single);
    }
}
