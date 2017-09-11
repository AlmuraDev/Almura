/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.item.group.factory;

import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.content.loader.StageTask;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.content.type.item.group.ItemGroupConfig;
import com.almuradev.almura.registry.CatalogDelegate;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.item.ItemType;

public class SetItemGroupAttributesTask implements StageTask<ItemGroup, ItemGroup.Builder> {

    @Override
    public void execute(AssetContext<ItemGroup, ItemGroup.Builder> context) {
        final Asset asset = context.getAsset();
        final ConfigurationNode root = asset.getConfigurationNode();

        final ConfigurationNode iconNode = root.getNode(ItemGroupConfig.ICON);
        if (!iconNode.isVirtual()) {
            context.getBuilder().icon(new CatalogDelegate<>(ItemType.class, iconNode.getString()));
        }
    }
}
