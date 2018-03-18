/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling;

import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.BlockModule;
import com.almuradev.content.type.block.type.sapling.processor.BigTreeProcessor;
import com.almuradev.content.type.block.type.sapling.processor.TreeProcessor;

public final class SaplingBlockModule extends BlockModule.Module {
    @Override
    protected void configure() {
        this.bind(SaplingBlock.Builder.class).to(SaplingBlockBuilder.class);
        this.processors()
                .only(BigTreeProcessor.class, BlockGenre.SAPLING)
                .only(TreeProcessor.class, BlockGenre.SAPLING);
    }
}
