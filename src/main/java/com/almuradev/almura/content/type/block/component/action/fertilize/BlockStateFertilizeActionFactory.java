/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.action.fertilize;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.almura.content.type.block.type.crop.state.CropBlockStateDefinitionBuilder;
import com.almuradev.shared.config.ConfigPath;
import ninja.leaping.configurate.ConfigurationNode;

public class BlockStateFertilizeActionFactory extends CropBlockStateDefinitionBuilder.Factory {

    @Override
    protected ConfigPath key() {
        return BlockConfig.State.Action.FERTILIZE;
    }

    @Override
    protected void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BuildableBlockType.Builder<?, ?> builder, final CropBlockStateDefinitionBuilder definition) {

    }
}
