/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.log;

import com.almuradev.content.type.block.BlockModule;

public final class LogBlockModule extends BlockModule.Module {
    @Override
    protected void configure() {
        this.bind(LogBlock.Builder.class).to(LogBlockBuilder.class);
    }
}
