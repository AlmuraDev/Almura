/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.processor;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.block.BlockConfig;
import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.mapcolor.MapColor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class MapColorProcessor implements BlockContentProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(BlockConfig.MAP_COLOR);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder) {
        builder.mapColor(CatalogDelegate.namespaced(MapColor.class, config));
    }
}
