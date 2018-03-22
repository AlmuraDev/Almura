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
import net.minecraft.util.BlockRenderLayer;
import ninja.leaping.configurate.ConfigurationNode;

public final class RenderLayerProcessor implements BlockContentProcessor.AnyTagged {

    private static final ConfigTag TAG = ConfigTag.create(BlockConfig.RENDER_LAYER);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(ConfigurationNode config,
            ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder) {
        final String rawRenderLayer = config.getString("cutout_mipped").toUpperCase();
        builder.renderLayer(BlockRenderLayer.valueOf(rawRenderLayer));
    }
}
