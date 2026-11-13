/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.processor;

import com.almuradev.content.type.item.ContentItem;
import com.almuradev.content.type.item.ItemConfig;
import com.almuradev.content.type.item.ItemContentProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class DurabilityProcessor implements ItemContentProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(ItemConfig.DURABILITY);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final ContentItem.Builder builder) {
        builder.durability(config.getInt());
    }
}
