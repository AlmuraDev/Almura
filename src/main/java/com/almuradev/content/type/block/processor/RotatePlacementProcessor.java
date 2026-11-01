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
import com.almuradev.content.type.block.PlacementRotation;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Locale;

/**
 * {@code rotate_placement}: {@code true} (or {@code "across"}) or {@code "toward"}, see {@link PlacementRotation}.
 */
public final class RotatePlacementProcessor implements BlockContentProcessor.State.Any {
    private static final ConfigTag TAG = ConfigTag.create(BlockConfig.State.ROTATE_PLACEMENT);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(final ConfigurationNode config, final ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder, final BlockStateDefinition.Builder<BlockStateDefinition> definition) {
        if (config.isVirtual()) {
            return;
        }
        final Object value = config.getValue();
        if (value instanceof Boolean) {
            if ((Boolean) value) {
                definition.rotatePlacement(PlacementRotation.ACROSS);
            }
        } else if (value != null) {
            definition.rotatePlacement(PlacementRotation.valueOf(value.toString().trim().toUpperCase(Locale.ROOT)));
        }
    }
}
