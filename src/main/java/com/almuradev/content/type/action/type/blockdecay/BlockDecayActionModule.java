/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdecay;

import com.almuradev.content.type.action.ActionGenre;
import com.almuradev.content.type.action.ActionModule;
import com.almuradev.content.type.action.type.blockdecay.processor.BlockDecayActionContentProcessor;

public final class BlockDecayActionModule extends ActionModule.Module {
    @Override
    protected void configure() {
        this.bind(BlockDecayAction.Builder.class).to(BlockDecayActionBuilder.class);
        this.processors()
                .only(BlockDecayActionContentProcessor.class, ActionGenre.BLOCK_DECAY);
    }
}
