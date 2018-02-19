/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling.processor;

import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.type.sapling.SaplingBlock;
import com.almuradev.content.type.block.type.sapling.state.SaplingBlockStateDefinition;
import com.almuradev.content.type.block.type.sapling.state.SaplingBlockStateDefinitionBuilder;

public interface SaplingBlockContentProcessor extends BlockContentProcessor<SaplingBlock, SaplingBlock.Builder, SaplingBlockStateDefinition, SaplingBlockStateDefinitionBuilder> {
    interface State extends BlockContentProcessor.State<SaplingBlock, SaplingBlock.Builder, SaplingBlockStateDefinition, SaplingBlockStateDefinitionBuilder> {
    }
}
