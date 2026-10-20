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
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

/**
 * {@code "sack_harvest": { "reset_age": 2 }}: the ripe crop can be picked with a harvesting sack, which collects its drops and sets
 * it back to {@code reset_age} instead of destroying it.
 */
public final class SackHarvestProcessor implements CropBlockContentProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(CropBlockConfig.SACK_HARVEST);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final CropBlock.Builder builder) {
        builder.sackHarvestResetAge(config.getNode(CropBlockConfig.SackHarvest.RESET_AGE).getInt(0));
    }
}
