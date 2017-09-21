/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.factory;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetFactory;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.shared.registry.catalog.CatalogDelegate;
import ninja.leaping.configurate.ConfigurationNode;

public class BlockItemGroupProvider implements AssetFactory<BuildableBlockType, BuildableBlockType.Builder<?, ?>> {

    @Override
    public void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BuildableBlockType.Builder<?, ?> builder) {
        if (config.getNode(BlockConfig.ATTRIBUTES, BlockConfig.Attribute.ITEM_GROUP_DISPLAY).getBoolean(true)) {
            builder.itemGroup(new CatalogDelegate<>(ItemGroup.class, pack.getId()));
        }
    }
}
