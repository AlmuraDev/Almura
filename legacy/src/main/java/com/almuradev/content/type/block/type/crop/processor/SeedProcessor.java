/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.processor;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.block.type.crop.CropBlock;
import com.almuradev.content.type.block.type.crop.CropBlockConfig;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.item.ItemType;

public final class SeedProcessor implements CropBlockContentProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(CropBlockConfig.SEED);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final CropBlock.Builder builder) {
        builder.seed(CatalogDelegate.namespaced(ItemType.class, config));
    }
}
