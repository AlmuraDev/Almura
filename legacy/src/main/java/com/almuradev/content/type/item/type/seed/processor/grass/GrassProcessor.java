/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed.processor.grass;

import com.almuradev.content.type.item.type.seed.SeedItem;
import com.almuradev.content.type.item.type.seed.SeedItemConfig;
import com.almuradev.content.type.item.type.seed.processor.AbstractSeedProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class GrassProcessor implements AbstractSeedProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(SeedItemConfig.GRASS);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final SeedItem.Builder context) {
        Grass.PARSER.deserialize(config).ifPresent(context::grass);
    }
}
