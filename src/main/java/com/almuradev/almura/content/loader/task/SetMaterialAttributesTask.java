/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.task;

import com.almuradev.almura.Constants;
import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.content.loader.CatalogDelegate;
import com.almuradev.almura.content.material.MaterialType;
import ninja.leaping.configurate.ConfigurationNode;

public class SetMaterialAttributesTask implements StageTask<MaterialType, MaterialType.Builder> {

    @Override
    public void execute(AssetContext<MaterialType, MaterialType.Builder> context) {
        final MaterialType.Builder builder = context.getBuilder();
        final ConfigurationNode node = context.getAsset().getConfigurationNode();

        final boolean displayInItemGroup = node.getNode(Constants.Config.GENERAL, Constants.Config.ITEM_GROUP_DISPLAY).getBoolean(true);

        if (displayInItemGroup) {
            builder.itemGroup(new CatalogDelegate<>(ItemGroup.class, context.getPack().getId()));
        }
    }
}
