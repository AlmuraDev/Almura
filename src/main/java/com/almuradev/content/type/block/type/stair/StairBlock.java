/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.stair;

import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.block.type.stair.state.StairBlockStateDefinition;
import com.almuradev.content.type.block.type.stair.state.StairBlockStateDefinitionBuilder;
import net.minecraft.block.state.IBlockState;

public interface StairBlock extends ContentBlock.InInventory {
    @Override
    StairBlockStateDefinition definition(final IBlockState state);

    interface Builder extends ContentBlock.Builder.Single<StairBlock, StairBlockStateDefinition, StairBlockStateDefinitionBuilder> {
        void block(final LazyBlockState block);
    }
}
