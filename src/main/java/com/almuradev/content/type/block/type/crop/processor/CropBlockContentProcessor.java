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

public interface CropBlockContentProcessor extends BlockContentProcessor<CropBlock, CropBlock.Builder, CropBlockStateDefinition, CropBlockStateDefinitionBuilder> {

    interface State extends BlockContentProcessor.State<CropBlock, CropBlock.Builder, CropBlockStateDefinition, CropBlockStateDefinitionBuilder> {

    }
}
