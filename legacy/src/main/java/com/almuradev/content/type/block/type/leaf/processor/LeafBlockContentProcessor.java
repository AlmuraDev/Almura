/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf.processor;

import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.type.leaf.LeafBlock;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinition;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinitionBuilder;

public interface LeafBlockContentProcessor extends BlockContentProcessor<LeafBlock, LeafBlock.Builder, LeafBlockStateDefinition, LeafBlockStateDefinitionBuilder> {

    interface AnyState extends BlockContentProcessor.State<LeafBlock, LeafBlock.Builder, LeafBlockStateDefinition, LeafBlockStateDefinitionBuilder> {

    }
}
