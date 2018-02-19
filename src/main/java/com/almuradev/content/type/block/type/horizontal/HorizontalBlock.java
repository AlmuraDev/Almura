/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal;

import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.type.horizontal.state.HorizontalBlockStateDefinition;
import com.almuradev.content.type.block.type.horizontal.state.HorizontalBlockStateDefinitionBuilder;
import net.minecraft.block.state.IBlockState;

public interface HorizontalBlock extends ContentBlock.InInventory {
    @Override
    HorizontalBlockStateDefinition definition(final IBlockState state);

    interface Builder extends ContentBlock.Builder<HorizontalBlock, HorizontalBlockStateDefinition, HorizontalBlockStateDefinitionBuilder> {
    }
}
