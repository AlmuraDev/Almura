/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.MaterialType;
import com.almuradev.almura.content.loader.AssetContext;
import ninja.leaping.configurate.ConfigurationNode;

public class SetCommonMaterialAttributesTask implements StageTask<MaterialType, MaterialType.Builder> {

    public static final SetCommonMaterialAttributesTask instance = new SetCommonMaterialAttributesTask();

    @Override
    public void execute(AssetContext<MaterialType, MaterialType.Builder> context) throws TaskExecutionFailedException {
        final MaterialType.Builder builder = context.getBuilder();
        final ConfigurationNode root = context.getAsset().getRoot();

        final boolean showInCreativeTab = root.getNode("general", "show-in-creative-tab").getBoolean(true);

        // TODO Figure out the creative tab
        if (showInCreativeTab) {

        }
    }
}
