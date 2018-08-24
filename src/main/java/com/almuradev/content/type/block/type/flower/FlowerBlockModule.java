/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.flower;

import com.almuradev.content.type.block.BlockModule;

public final class FlowerBlockModule extends BlockModule.Module {
    @Override
    protected void configure() {
        this.bind(FlowerBlock.Builder.class).to(FlowerBlockBuilder.class);
    }
}
