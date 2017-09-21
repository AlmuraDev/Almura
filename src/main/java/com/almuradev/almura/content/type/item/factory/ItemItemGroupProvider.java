/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.item.factory;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetFactory;
import com.almuradev.almura.content.type.item.ItemConfig;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.content.type.material.MaterialType;
import com.almuradev.shared.registry.catalog.CatalogDelegate;
import ninja.leaping.configurate.ConfigurationNode;

public class ItemItemGroupProvider implements AssetFactory<MaterialType, MaterialType.Builder> {

    @Override
    public void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final MaterialType.Builder builder) {
        if (config.getNode(ItemConfig.ATTRIBUTES, ItemConfig.Attribute.ITEM_GROUP_DISPLAY).getBoolean(true)) {
            builder.itemGroup(new CatalogDelegate<>(ItemGroup.class, pack.getId()));
        }
    }
}
