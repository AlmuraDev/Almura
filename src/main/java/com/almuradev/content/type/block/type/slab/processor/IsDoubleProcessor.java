/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.slab.processor;

import com.almuradev.content.type.block.type.slab.SlabBlock;
import com.almuradev.content.type.block.type.slab.SlabBlockConfig;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class IsDoubleProcessor implements AbstractSlabProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(SlabBlockConfig.IS_DOUBLE);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final SlabBlock.Builder builder) {
        builder.isDouble(config.getBoolean(false));
    }
}
