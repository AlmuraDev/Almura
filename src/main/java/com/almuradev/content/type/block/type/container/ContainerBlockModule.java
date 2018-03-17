/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container;

import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.block.BlockModule;
import com.almuradev.content.type.block.type.container.processor.LimitProcessor;

public final class ContainerBlockModule extends BlockModule.Module {
    @Override
    protected void configure() {
        this.bind(ContainerBlock.Builder.class).to(ContainerBlockBuilder.class);
        this.processors()
                .only(LimitProcessor.class, BlockGenre.CONTAINER);
    }
}
