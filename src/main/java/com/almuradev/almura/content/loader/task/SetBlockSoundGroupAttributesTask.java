/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.task;

import com.almuradev.almura.content.block.sound.BlockSoundGroup;
import com.almuradev.almura.content.block.sound.BlockSoundGroupBuilder;
import com.almuradev.almura.content.loader.AssetContext;
import ninja.leaping.configurate.ConfigurationNode;

public class SetBlockSoundGroupAttributesTask implements StageTask<BlockSoundGroup, BlockSoundGroup.Builder> {

    @Override
    public void execute(AssetContext<BlockSoundGroup, BlockSoundGroup.Builder> context) {
        final BlockSoundGroup.Builder builder = context.getBuilder();
        final ConfigurationNode node = context.getAsset().getConfigurationNode();

        BlockSoundGroupBuilder.fill(builder, context.getAsset().getName(), node);
    }
}
