/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.slab.processor;

import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.type.slab.SlabBlock;
import com.almuradev.content.type.block.type.slab.state.SlabBlockStateDefinition;
import com.almuradev.content.type.block.type.slab.state.SlabBlockStateDefinitionBuilder;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

public interface AbstractSlabProcessor extends BlockContentProcessor<SlabBlock, SlabBlock.Builder, SlabBlockStateDefinition, SlabBlockStateDefinitionBuilder> {
    interface AnyTagged extends AbstractSlabProcessor, TaggedConfigProcessor<SlabBlock.Builder, ConfigTag> {
    }

    interface State extends BlockContentProcessor.State<SlabBlock, SlabBlock.Builder, SlabBlockStateDefinition, SlabBlockStateDefinitionBuilder> {
    }
}
