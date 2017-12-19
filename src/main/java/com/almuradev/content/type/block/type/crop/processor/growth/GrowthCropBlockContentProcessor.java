/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.processor.growth;

import com.almuradev.content.type.block.type.crop.CropBlock;
import com.almuradev.content.type.block.type.crop.CropBlockConfig;
import com.almuradev.content.type.block.type.crop.processor.CropBlockContentProcessor;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinitionBuilder;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public final class GrowthCropBlockContentProcessor implements CropBlockContentProcessor.State {

    private static final ConfigTag TAG = ConfigTag.create(CropBlockConfig.State.GROWTH);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(final ConfigurationNode config, final CropBlock.Builder builder, final CropBlockStateDefinitionBuilder definition) {
        Growth.PARSER.deserialize(config).ifPresent(definition::growth);
    }
}
