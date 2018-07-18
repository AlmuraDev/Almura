/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.processor;

import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.type.crop.CropBlock;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinition;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinitionBuilder;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

public interface CropBlockContentProcessor extends BlockContentProcessor<CropBlock, CropBlock.Builder, CropBlockStateDefinition, CropBlockStateDefinitionBuilder> {
    interface AnyTagged extends CropBlockContentProcessor, TaggedConfigProcessor<CropBlock.Builder, ConfigTag> {
    }

    interface State extends BlockContentProcessor.State<CropBlock, CropBlock.Builder, CropBlockStateDefinition, CropBlockStateDefinitionBuilder> {
    }
}
