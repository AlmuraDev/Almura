/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.almuradev.toolbox.config.processor.MappedConfigProcessor;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

public interface BlockContentProcessor<C extends ContentBlock, B extends ContentBlock.Builder<C, S, D>, S extends BlockStateDefinition, D extends BlockStateDefinition.Builder<S>> extends ConfigProcessor<B> {
    interface Any extends BlockContentProcessor<ContentBlock, ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> {
    }

    interface AnyTagged extends BlockContentProcessor<ContentBlock, ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, TaggedConfigProcessor<ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, ConfigTag> {
    }

    interface State<C extends ContentBlock, B extends ContentBlock.Builder<C, S, D>, S extends BlockStateDefinition, D extends BlockStateDefinition.Builder<S>> extends BlockContentProcessor<C, B, S, D>, MappedConfigProcessor<B, ConfigTag> {
        @Override
        default ConfigurationNode config(final ConfigurationNode config) {
            return config.getNode(BlockConfig.STATE);
        }

        @Override
        default void processTagged(final String id, final ConfigurationNode config, final B builder) {
            final D definition = builder.stateBuilder(id);
            this.processState(config, builder, definition);
        }

        @Override
        default void postProcessTagged(final String id, final ConfigurationNode config, final B builder) {
            final D definition = builder.stateBuilder(id);
            this.postProcessState(config, builder, definition);
        }

        void processState(final ConfigurationNode config, final B builder, final D definition);

        default void postProcessState(final ConfigurationNode config, final B builder, final D definition) {
        }

        interface Any extends BlockContentProcessor.State<ContentBlock, ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> {
        }
    }
}
