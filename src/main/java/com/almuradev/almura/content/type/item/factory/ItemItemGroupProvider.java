/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.item.factory;

import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.content.loader.StageTask;
import com.almuradev.almura.content.type.item.ItemConfig;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.content.type.material.MaterialType;
import com.almuradev.shared.registry.catalog.CatalogDelegate;

public class ItemItemGroupProvider implements StageTask<MaterialType, MaterialType.Builder> {

    @Override
    public void execute(final AssetContext<MaterialType, MaterialType.Builder> context) {
        if (context.getAsset().getConfigurationNode().getNode(ItemConfig.ATTRIBUTES, ItemConfig.Attribute.ITEM_GROUP_DISPLAY).getBoolean(true)) {
            context.getBuilder().itemGroup(new CatalogDelegate<>(ItemGroup.class, context.getPack().getId()));
        }
    }
}
