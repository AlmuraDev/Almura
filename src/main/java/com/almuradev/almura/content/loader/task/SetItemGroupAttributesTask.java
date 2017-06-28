/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.task;

import com.almuradev.almura.Constants;
import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetContext;
import ninja.leaping.configurate.ConfigurationNode;

public class SetItemGroupAttributesTask implements StageTask<ItemGroup, ItemGroup.Builder> {

    @Override
    public void execute(AssetContext<ItemGroup, ItemGroup.Builder> context) {
        final Asset asset = context.getAsset();
        final ConfigurationNode root = asset.getConfigurationNode();

        // TODO If tab label is virtual, use filename
        final String tabLabel = root.getNode(Constants.Config.GENERAL, Constants.Config.ItemGroup.LABEL).getString("");

    }
}
