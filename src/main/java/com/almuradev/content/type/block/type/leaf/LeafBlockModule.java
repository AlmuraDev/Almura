/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf;

import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.BlockModule;
import com.almuradev.content.type.block.type.leaf.processor.DecayActionProcessor;

public final class LeafBlockModule extends BlockModule.Module {
    @Override
    protected void configure() {
        this.bind(LeafBlock.Builder.class).to(LeafBlockBuilder.class);
        this.processors().only(DecayActionProcessor.class, BlockGenre.LEAF);
    }
}
