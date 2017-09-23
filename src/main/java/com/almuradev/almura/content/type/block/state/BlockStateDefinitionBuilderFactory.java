/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.block.state;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetFactory;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.shared.config.ConfigurationNodes;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;

import java.util.Map;

import javax.inject.Inject;

public abstract class BlockStateDefinitionBuilderFactory implements AssetFactory<BuildableBlockType, BuildableBlockType.Builder<?, ?>> {

    @Inject private Logger logger;

    @Override
    public final void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BuildableBlockType.Builder<?, ?> builder) {
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : config.getNode(BlockConfig.STATES).getChildrenMap().entrySet()) {
            final ConfigurationNode dc = entry.getValue();
            final BlockStateDefinitionBuilder<?> definition = builder.findState(String.valueOf(entry.getKey())).orElseGet(() -> {
                final BlockStateDefinitionBuilder<?> def = BlockStateDefinitionBuilder.create();
                def.id(asset.getName(), String.valueOf(entry.getKey()));
                ConfigurationNodes.whenRealString(dc.getNode(BlockConfig.State.PARENT_KEY), parentId -> builder.findState(parentId).ifPresent(def::from));
                builder.putState(def);
                return def;
            });
            this.configure(pack, asset, dc.getNode(this.key()), builder, definition);
        }
    }

    protected abstract String key();

    protected abstract void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BuildableBlockType.Builder<?, ?> builder, final BlockStateDefinitionBuilder<?> definition);

    protected final void missingData(final Asset asset, final String what) {
        this.logger.debug("Block '{}' at '{}' does not have a {}", asset.getName(), asset.getPath().toString(), what);
    }
}
