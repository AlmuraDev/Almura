/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.slab.processor;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.block.type.slab.SlabBlock;
import com.almuradev.content.type.block.type.slab.SlabBlockConfig;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class SingleProcessor implements AbstractSlabProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(SlabBlockConfig.SINGLE);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final SlabBlock.Builder builder) {
        builder.single(LazyBlockState.parse(config));
    }
}
