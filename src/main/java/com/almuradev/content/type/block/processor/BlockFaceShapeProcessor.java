package com.almuradev.content.type.block.processor;

import com.almuradev.content.type.block.BlockConfig;
import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.ContentBlockType;
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
    public void processState(ConfigurationNode config,
            ContentBlockType.Builder<ContentBlockType, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder,
            BlockStateDefinition.Builder<BlockStateDefinition> definition) {
        final BlockFaceShape blockFaceShape = BlockFaceShape.valueOf(config.getString("undefined").toUpperCase().replace(' ', '_'));
        definition.blockFaceShape(blockFaceShape);
    }
}
