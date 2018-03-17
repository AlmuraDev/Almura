/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container.processor;

import com.almuradev.content.type.block.type.container.ContainerBlock;
import com.almuradev.content.type.block.type.container.ContainerBlockConfig;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class LimitProcessor implements ContainerBlockContentProcessor.Tagged {
    private static final ConfigTag TAG = ConfigTag.create(ContainerBlockConfig.LIMIT);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final ContainerBlock.Builder builder) {
        builder.limit(config.getInt());
    }
}
