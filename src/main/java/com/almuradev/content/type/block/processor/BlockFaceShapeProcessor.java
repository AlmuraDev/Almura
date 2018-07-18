/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.processor;

import com.almuradev.content.type.block.BlockConfig;
import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.toolbox.config.tag.ConfigTag;
import net.minecraft.block.state.BlockFaceShape;
import ninja.leaping.configurate.ConfigurationNode;

public final class BlockFaceShapeProcessor implements BlockContentProcessor.State.Any {
    private static final ConfigTag TAG = ConfigTag.create(BlockConfig.State.BLOCK_FACE_SHAPE);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(ConfigurationNode config, final ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder, final BlockStateDefinition.Builder<BlockStateDefinition> definition) {
        final BlockFaceShape shape = BlockFaceShape.valueOf(config.getString("undefined").toUpperCase().replace(' ', '_'));
        definition.blockFaceShape(shape);
    }
}
