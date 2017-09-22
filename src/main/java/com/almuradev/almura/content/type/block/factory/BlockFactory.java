/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.factory;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetFactory;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.BlockType;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import ninja.leaping.configurate.ConfigurationNode;

public interface BlockFactory<T extends BuildableBlockType, B extends BuildableBlockType.Builder<?, ?>> extends AssetFactory<T, B> {

    @Override
    default void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final B builder) {
        this.configure(pack, asset, config, BlockType.of(config.getNode(BlockConfig.TYPE).getString()), builder);
    }

    void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BlockType type, final B builder);
}
