/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.state.factory;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.BlockType;
import com.almuradev.almura.content.type.block.factory.BlockFactory;
import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.shared.config.ConfigPath;
import com.almuradev.shared.config.ConfigurationNodes;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;

import java.util.Map;

import javax.inject.Inject;

/**
 * @param <S> The block state definition builder type.
 */
public abstract class AbstractBlockStateDefinitionBuilderFactory<S extends BlockStateDefinitionBuilder<?>> implements BlockFactory<BuildableBlockType, BuildableBlockType.Builder<?, ?>> {

    @Inject private Logger logger;

    @Override
    public void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BlockType type, final BuildableBlockType.Builder<?, ?> builder) {
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : config.getNode(BlockConfig.STATES).getChildrenMap().entrySet()) {
            final ConfigurationNode dc = entry.getValue();
            final S definition = builder.<S>findState(String.valueOf(entry.getKey())).orElseGet(() -> {
                final S def = (S) type.builder();
                def.id(asset.getName(), String.valueOf(entry.getKey()));
                ConfigurationNodes.whenRealString(dc.getNode(BlockConfig.State.PARENT_KEY), parentId -> builder.findState(parentId).ifPresent(def::from));
                builder.putState(def);
                return def;
            });
            this.configure(pack, asset, this.key().in(dc), builder, definition);
        }
    }

    protected abstract ConfigPath key();

    protected abstract void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BuildableBlockType.Builder<?, ?> builder, final S definition);

    protected final void missingData(final Asset asset, final String what) {
        this.logger.debug("Block '{}' at '{}' does not have a {}", asset.getName(), asset.getPath().toString(), what);
    }
}
