/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.item.group.factory;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetFactory;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.content.type.item.group.ItemGroupConfig;
import com.almuradev.shared.registry.catalog.CatalogDelegate;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.item.ItemType;

public class ItemGroupFactory implements AssetFactory<ItemGroup, ItemGroup.Builder> {

    @Override
    public void configure(Pack pack, Asset asset, ConfigurationNode config, ItemGroup.Builder builder) {
        final ConfigurationNode iconNode = config.getNode(ItemGroupConfig.ICON);
        if (!iconNode.isVirtual()) {
            builder.icon(new CatalogDelegate<>(ItemType.class, iconNode.getString()));
        }
    }
}
