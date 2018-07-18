/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed.processor;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.item.type.seed.SeedItem;
import com.almuradev.content.type.item.type.seed.SeedItemConfig;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockType;

public final class CropProcessor implements AbstractSeedProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(SeedItemConfig.CROP);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final SeedItem.Builder builder) {
        builder.crop(CatalogDelegate.namespaced(BlockType.class, config));
    }
}
