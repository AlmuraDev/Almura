/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling;

import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.type.sapling.state.SaplingBlockStateDefinition;
import com.almuradev.content.type.block.type.sapling.state.SaplingBlockStateDefinitionBuilder;
import net.minecraft.block.state.IBlockState;

public interface SaplingBlock extends ContentBlock.InInventory {
    @Override
    SaplingBlockStateDefinition definition(final IBlockState state);

    interface Builder extends ContentBlock.Builder.Single<SaplingBlock, SaplingBlockStateDefinition, SaplingBlockStateDefinitionBuilder> {
    }
}
