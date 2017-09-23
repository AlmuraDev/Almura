/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.block.component.aabb.factory;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.component.aabb.CollisionBox;
import com.almuradev.almura.content.type.block.component.aabb.WireFrame;
import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilderFactory;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import ninja.leaping.configurate.ConfigurationNode;

public class BlockStateAABBFactory extends BlockStateDefinitionBuilderFactory {

    @Override
    public String key() {
        return BlockConfig.State.AABB_KEY;
    }

    @Override
    public void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BuildableBlockType.Builder<?, ?> builder, final BlockStateDefinitionBuilder<?> definition) {
        if (config.isVirtual()) {
            return;
        }
        CollisionBox.deserialize(config.getNode(BlockConfig.State.AABB.COLLISION)).ifPresent(definition::collisionAABB);
        WireFrame.deserialize(config.getNode(BlockConfig.State.AABB.WIREFRAME)).ifPresent(definition::wireFrameAABB);
    }
}
