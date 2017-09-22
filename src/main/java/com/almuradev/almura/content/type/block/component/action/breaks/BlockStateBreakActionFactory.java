/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.action.breaks;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.block.state.factory.GenericBlockStateDefinitionBuilderFactory;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.shared.config.ConfigPath;
import ninja.leaping.configurate.ConfigurationNode;

public class BlockStateBreakActionFactory extends GenericBlockStateDefinitionBuilderFactory {

    @Override
    protected ConfigPath key() {
        return BlockConfig.State.Action.BREAK;
    }

    @Override
    protected void configure(final Pack pack, final Asset asset, ConfigurationNode config, final BuildableBlockType.Builder<?, ?> builder, final BlockStateDefinitionBuilder<?> definition) {
        if (!config.isVirtual()) {
            BlockBreakSerializer.INSTANCE.deserialize(config).ifPresent(definition::breaks);
        }
    }
}
