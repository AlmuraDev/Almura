/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop;

import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinition;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinitionBuilder;
import net.minecraft.block.state.IBlockState;

public interface CropBlock extends ContentBlockType {
    @Override
    CropBlockStateDefinition definition(final IBlockState state);

    interface Builder extends ContentBlockType.Builder<CropBlock, CropBlockStateDefinition, CropBlockStateDefinitionBuilder> {
    }
}
