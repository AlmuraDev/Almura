/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf.processor.spread;

import com.almuradev.content.type.block.type.leaf.LeafBlock;
import com.almuradev.content.type.block.type.leaf.LeafBlockConfig;
import com.almuradev.content.type.block.type.leaf.processor.LeafBlockContentProcessor;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinitionBuilder;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class SpreadProcessor implements LeafBlockContentProcessor.AnyState {
    private static final ConfigTag TAG = ConfigTag.create(LeafBlockConfig.SPREAD);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(ConfigurationNode config, LeafBlock.Builder builder, LeafBlockStateDefinitionBuilder definition) {
        Spread.PARSER.deserialize(config).ifPresent(definition::spread);
    }
}
