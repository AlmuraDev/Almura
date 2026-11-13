/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.processor;

import com.almuradev.content.type.block.type.crop.CropBlock;
import com.almuradev.content.type.block.type.crop.CropBlockConfig;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinitionBuilder;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public class CanRollbackProcessor implements CropBlockContentProcessor.State {
    private static final ConfigTag TAG = ConfigTag.create(CropBlockConfig.State.CAN_ROLLBACK);
    private static final boolean DEFAULT_VALUE = true;

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(final ConfigurationNode config, final CropBlock.Builder builder, final CropBlockStateDefinitionBuilder definition) {
        definition.canRollback(config.getBoolean(DEFAULT_VALUE));
    }
}
