/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling.processor;

import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.block.type.sapling.SaplingBlock;
import com.almuradev.content.type.block.type.sapling.SaplingBlockConfig;
import com.almuradev.content.type.block.type.sapling.state.SaplingBlockStateDefinitionBuilder;
import com.almuradev.content.type.tree.Tree;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class TreeProcessor implements SaplingBlockContentProcessor.State {
    private static final ConfigTag TAG = ConfigTag.create(SaplingBlockConfig.TREE);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public void processState(final ConfigurationNode config, final SaplingBlock.Builder builder, final SaplingBlockStateDefinitionBuilder definition) {
        definition.tree(CatalogDelegate.namespaced(Tree.class, config));
    }
}
