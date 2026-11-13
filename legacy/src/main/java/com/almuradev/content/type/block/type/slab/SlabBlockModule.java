/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.slab;

import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.BlockModule;
import com.almuradev.content.type.block.type.slab.processor.IsDoubleProcessor;
import com.almuradev.content.type.block.type.slab.processor.SingleProcessor;

public final class SlabBlockModule extends BlockModule.Module {
    @Override
    protected void configure() {
        this.bind(SlabBlock.Builder.class).to(SlabBlockBuilder.class);
        this.processors()
                .only(IsDoubleProcessor.class, BlockGenre.SLAB)
                .only(SingleProcessor.class, BlockGenre.SLAB);
    }
}
