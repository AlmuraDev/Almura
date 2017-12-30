/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal;

import com.almuradev.content.type.block.BlockModule;

public final class HorizontalBlockModule extends BlockModule.Module {

    @Override
    protected void configure() {
        this.bind(HorizontalBlock.Builder.class).to(HorizontalBlockBuilder.class);
    }
}
