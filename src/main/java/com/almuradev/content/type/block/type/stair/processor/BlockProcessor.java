/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.stair.processor;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.block.type.stair.StairBlock;
import com.almuradev.content.type.block.type.stair.StairBlockConfig;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class BlockProcessor implements AbstractStairProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(StairBlockConfig.BLOCK);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final StairBlock.Builder builder) {
        builder.block(LazyBlockState.parse(config));
    }
}
