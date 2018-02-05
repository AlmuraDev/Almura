/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.processor;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.item.type.tool.ToolItem;
import com.almuradev.content.type.item.type.tool.ToolItemConfig;
import com.almuradev.content.type.item.type.tool.ToolItemProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class TierProcessor implements ToolItemProcessor.AnyTagged<ToolItem, ToolItem.Builder<ToolItem>> {
    private static final ConfigTag TAG = ConfigTag.create(ToolItemConfig.TIER);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final ToolItem.Builder builder) {
        builder.tier(CatalogDelegate.namespaced(ToolItem.Tier.class, config.getString()));
    }
}
