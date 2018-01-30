/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.normal;

import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.content.type.block.type.normal.state.NormalBlockStateDefinition;
import com.almuradev.content.type.block.type.normal.state.NormalBlockStateDefinitionBuilder;
import net.minecraft.block.state.IBlockState;

public interface NormalBlock extends ContentBlockType.InInventory {
    @Override
    NormalBlockStateDefinition definition(final IBlockState state);

    interface Builder extends ContentBlockType.Builder.Single<NormalBlock, NormalBlockStateDefinition, NormalBlockStateDefinitionBuilder> {

    }
}
