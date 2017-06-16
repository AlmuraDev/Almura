/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.Constants;
import com.almuradev.almura.block.BuildableBlockType;
import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.AssetContext;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Locale;

public class SetCommonBlockAttributesTask implements StageTask<BuildableBlockType, BuildableBlockType.Builder> {

    public static final SetCommonBlockAttributesTask instance = new SetCommonBlockAttributesTask();

    @Override
    public void execute(AssetContext<BuildableBlockType, BuildableBlockType.Builder> context) throws TaskExecutionFailedException {
        final BuildableBlockType.Builder builder = context.getBuilder();
        final ConfigurationNode node = context.getAsset().getConfigurationNode();
        final ConfigurationNode generalNode = node.getNode(Constants.Config.BlockState.GENERAL);
        builder.hardness(generalNode.getNode(Constants.Config.BlockState.HARDNESS).getFloat(1f));

        final ConfigurationNode resistanceNode = generalNode.getNode(Constants.Config.BlockState.RESISTANCE);
        if (!resistanceNode.isVirtual()) {
            builder.resistance(resistanceNode.getFloat());
        }
        final Pack pack = context.getPack();
        builder.build(Constants.Plugin.ID + ":" + pack.getName().toLowerCase(Locale.ENGLISH) + "/" + context.getAsset().getName());
    }
}
