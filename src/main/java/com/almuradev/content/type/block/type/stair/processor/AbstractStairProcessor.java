/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.stair.processor;

import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.type.stair.StairBlock;
import com.almuradev.content.type.block.type.stair.state.StairBlockStateDefinition;
import com.almuradev.content.type.block.type.stair.state.StairBlockStateDefinitionBuilder;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

public interface AbstractStairProcessor extends BlockContentProcessor<StairBlock, StairBlock.Builder, StairBlockStateDefinition, StairBlockStateDefinitionBuilder> {
    interface AnyTagged extends AbstractStairProcessor, TaggedConfigProcessor<StairBlock.Builder, ConfigTag> {
    }

    interface State extends BlockContentProcessor.State<StairBlock, StairBlock.Builder, StairBlockStateDefinition, StairBlockStateDefinitionBuilder> {
    }
}
