/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container.processor;

import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.type.container.ContainerBlock;
import com.almuradev.content.type.block.type.container.state.ContainerBlockStateDefinition;
import com.almuradev.content.type.block.type.container.state.ContainerBlockStateDefinitionBuilder;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

public interface ContainerBlockContentProcessor extends BlockContentProcessor<ContainerBlock, ContainerBlock.Builder, ContainerBlockStateDefinition, ContainerBlockStateDefinitionBuilder> {

    interface Tagged extends ContainerBlockContentProcessor, TaggedConfigProcessor<ContainerBlock.Builder, ConfigTag> {

    }

    interface State extends BlockContentProcessor.State<ContainerBlock, ContainerBlock.Builder, ContainerBlockStateDefinition, ContainerBlockStateDefinitionBuilder> {

    }
}
