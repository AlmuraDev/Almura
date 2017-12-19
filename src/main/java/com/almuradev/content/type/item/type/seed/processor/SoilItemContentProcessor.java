/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed.processor;

import com.almuradev.almura.shared.registry.ResourceLocations;
import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.item.type.seed.SeedItem;
import com.almuradev.content.type.item.type.seed.SeedItemConfig;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockType;

public final class SoilItemContentProcessor implements TaggedConfigProcessor<SeedItem.Builder, ConfigTag> {

    private static final ConfigTag TAG = ConfigTag.create(SeedItemConfig.SOIL);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final SeedItem.Builder builder) {
        builder.soil(CatalogDelegate.create(BlockType.class, ResourceLocations.requireNamespaced(config)));
    }
}
