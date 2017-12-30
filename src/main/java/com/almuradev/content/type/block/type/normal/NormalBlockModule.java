/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.normal;

import com.almuradev.content.type.block.BlockModule;

public final class NormalBlockModule extends BlockModule.Module {

    @Override
    protected void configure() {
        this.bind(NormalBlock.Builder.class).to(NormalBlockBuilder.class);
    }
}
