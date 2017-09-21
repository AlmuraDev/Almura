/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.sound.factory;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetFactory;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroupBuilder;
import ninja.leaping.configurate.ConfigurationNode;

public class BlockSoundGroupFactory implements AssetFactory<BlockSoundGroup, BlockSoundGroup.Builder> {

    @Override
    public void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BlockSoundGroup.Builder builder) {
        BlockSoundGroupBuilder.fill(builder, asset.getName(), config);
    }
}
