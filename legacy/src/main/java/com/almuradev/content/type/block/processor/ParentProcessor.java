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
import ninja.leaping.configurate.ConfigurationNode;

public final class ParentProcessor implements BlockContentProcessor.State.Any {
    private static final ConfigTag TAG = ConfigTag.create(BlockConfig.State.PARENT);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(final ConfigurationNode config, final ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder, final BlockStateDefinition.Builder<BlockStateDefinition> definition) {
        // NOOP
    }

    @Override
    public void postProcessState(final ConfigurationNode config, final ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder, final BlockStateDefinition.Builder<BlockStateDefinition> definition) {
        builder.optionalStateBuilder(config.getString()).ifPresent(definition::parent);
    }
}
