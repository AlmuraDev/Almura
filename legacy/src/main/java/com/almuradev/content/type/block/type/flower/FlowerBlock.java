/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.flower;

import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.type.flower.state.FlowerBlockStateDefinition;
import com.almuradev.content.type.block.type.flower.state.FlowerBlockStateDefinitionBuilder;
import net.minecraft.block.state.IBlockState;

public interface FlowerBlock extends ContentBlock.InInventory {
    @Override
    FlowerBlockStateDefinition definition(final IBlockState state);

    interface Builder extends ContentBlock.Builder.Single<FlowerBlock, FlowerBlockStateDefinition, FlowerBlockStateDefinitionBuilder> {
    }
}
