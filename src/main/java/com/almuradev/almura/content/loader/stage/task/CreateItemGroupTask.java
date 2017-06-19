/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.Constants;
import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetContext;
import ninja.leaping.configurate.ConfigurationNode;

public class CreateItemGroupTask implements StageTask<ItemGroup, ItemGroup.Builder> {

    public static final CreateItemGroupTask instance = new CreateItemGroupTask();

    @Override
    public void execute(AssetContext<ItemGroup, ItemGroup.Builder> context) throws TaskExecutionFailedException {
        final Asset asset = context.getAsset();
        final ConfigurationNode root = asset.getConfigurationNode();
        final String tabLabel = root.getNode(Constants.Config.GENERAL, Constants.Config.ItemGroup.LABEL).getString("");
        if (tabLabel.isEmpty()) {
            throw new TaskExecutionFailedException("Tab label cannot be empty!");
        }
        context.setCatalog(context.getBuilder().build(asset.getName(), tabLabel));
    }
}
