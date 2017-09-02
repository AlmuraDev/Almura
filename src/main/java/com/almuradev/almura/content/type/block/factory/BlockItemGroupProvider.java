/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.factory;

import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.content.loader.task.StageTask;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.registry.CatalogDelegate;

public class BlockItemGroupProvider implements StageTask<BuildableBlockType, BuildableBlockType.Builder> {

    @Override
    public void execute(final AssetContext<BuildableBlockType, BuildableBlockType.Builder> context) {
        if (context.getAsset().getConfigurationNode().getNode(BlockConfig.ATTRIBUTES, BlockConfig.Attribute.ITEM_GROUP_DISPLAY).getBoolean(true)) {
            context.getBuilder().itemGroup(new CatalogDelegate<>(ItemGroup.class, context.getPack().getId()));
        }
    }
}
