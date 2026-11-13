/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.slab.processor;

import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.item.type.slab.SlabItem;
import com.almuradev.content.type.item.type.slab.SlabItemConfig;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class DoubleProcessor implements AbstractSlabProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(SlabItemConfig.DOUBLE);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final SlabItem.Builder builder) {
        builder.doubleBlock(LazyBlockState.parse(config));
    }
}
