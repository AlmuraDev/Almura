/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.stair;

import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.BlockModule;
import com.almuradev.content.type.block.type.stair.processor.BlockProcessor;

public final class StairBlockModule extends BlockModule.Module {
    @Override
    protected void configure() {
        this.bind(StairBlock.Builder.class).to(StairBlockBuilder.class);
        this.processors()
                .only(BlockProcessor.class, BlockGenre.STAIR);
    }
}
