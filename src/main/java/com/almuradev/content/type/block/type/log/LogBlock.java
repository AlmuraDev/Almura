/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.log;

import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.type.log.state.LogBlockStateDefinition;
import com.almuradev.content.type.block.type.log.state.LogBlockStateDefinitionBuilder;
import net.minecraft.block.state.IBlockState;

public interface LogBlock extends ContentBlock.InInventory {
    @Override
    LogBlockStateDefinition definition(final IBlockState state);

    interface Builder extends ContentBlock.Builder.Single<LogBlock, LogBlockStateDefinition, LogBlockStateDefinitionBuilder> {
    }
}
