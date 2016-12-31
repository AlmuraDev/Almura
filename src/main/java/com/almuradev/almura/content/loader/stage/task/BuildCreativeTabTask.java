/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.creativetab.CreativeTab;
import ninja.leaping.configurate.ConfigurationNode;

public class BuildCreativeTabTask implements StageTask<CreativeTab, CreativeTab.Builder> {

    public static final BuildCreativeTabTask instance = new BuildCreativeTabTask();

    @Override
    public void execute(AssetContext<CreativeTab, CreativeTab.Builder> context) throws TaskExecutionFailedException {
        final Asset asset = context.getAsset();
        final ConfigurationNode root = asset.getRoot();
        final String tabLabel = root.getNode("general", "label").getString("");
        if (tabLabel.isEmpty()) {
            throw new TaskExecutionFailedException("Tab label cannot be empty!");
        }
        context.setCatalog(context.getBuilder().build(asset.getName(), tabLabel));
    }
}
