/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.action.factory;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.component.action.breaks.BlockBreakSerializer;
import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.block.state.factory.BlockStateDefinitionBuilderFactory;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import ninja.leaping.configurate.ConfigurationNode;

public class BlockStateActionFactory extends BlockStateDefinitionBuilderFactory {

    @Override
    protected String key() {
        return BlockConfig.State.ACTION_KEY;
    }

    @Override
    protected void configure(final Pack pack, final Asset asset, ConfigurationNode config, final BuildableBlockType.Builder<?, ?> builder, final BlockStateDefinitionBuilder<?> definition) {
        final ConfigurationNode breaks = config.getNode(BlockConfig.State.Action.BREAK);
        if (!breaks.isVirtual()) {
            BlockBreakSerializer.INSTANCE.deserialize(breaks).ifPresent(definition::breaks);
        }
    }
}
