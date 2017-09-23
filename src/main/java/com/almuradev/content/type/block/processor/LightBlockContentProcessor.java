/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.processor;

import static com.almuradev.toolbox.config.ConfigurationNodes.whenRealFloat;
import static com.almuradev.toolbox.config.ConfigurationNodes.whenRealInt;

import com.almuradev.content.type.block.BlockConfig;
import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class LightBlockContentProcessor implements BlockContentProcessor.State.Any {

    private static final ConfigTag TAG = ConfigTag.create(BlockConfig.State.LIGHT);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(final ConfigurationNode config, final ContentBlockType.Builder<ContentBlockType, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder, final BlockStateDefinition.Builder<BlockStateDefinition> definition) {
        whenRealFloat(config.getNode(BlockConfig.State.Light.EMISSION), emission -> definition.lightEmission((float) emission));
        whenRealInt(config.getNode(BlockConfig.State.Light.OPACITY), definition::lightOpacity);
    }
}
