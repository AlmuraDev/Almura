/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdestroy;

import com.almuradev.content.type.action.ActionGenre;
import com.almuradev.content.type.action.ActionModule;
import com.almuradev.content.type.action.type.blockdestroy.processor.BlockDestroyActionContentProcessor;

public final class BlockDestroyActionModule extends ActionModule.Module {
    @Override
    protected void configure() {
        this.bind(BlockDestroyAction.Builder.class).to(BlockDestroyActionBuilder.class);
        this.processors()
                .only(BlockDestroyActionContentProcessor.class, ActionGenre.BLOCK_DESTROY);
    }
}
