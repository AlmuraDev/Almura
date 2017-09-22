/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.sound.factory;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroupBuilder;
import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.block.state.factory.GenericBlockStateDefinitionBuilderFactory;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.shared.config.ConfigPath;
import com.almuradev.shared.registry.catalog.CatalogDelegate;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.UUID;

public class BlockStateSoundGroupFactory extends GenericBlockStateDefinitionBuilderFactory {

    @Override
    public ConfigPath key() {
        return BlockConfig.State.SOUND_KEY;
    }

    @Override
    public void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BuildableBlockType.Builder<?, ?> builder, final BlockStateDefinitionBuilder<?> definition) {
        if (!config.isVirtual()) {
            if (config.getValue() instanceof String) {
                definition.soundGroup(new CatalogDelegate<>(BlockSoundGroup.class, config.getString()));
            } else {
                final String id = UUID.randomUUID().toString().replace("-", "");
                definition.soundGroup(CatalogDelegate.of(BlockSoundGroupBuilder.createVirtual(id, config)));
            }
        } else {
            this.missingData(asset, BlockConfig.State.Generic.SOUND_GROUP);
        }
    }
}
