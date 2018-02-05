/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.processor;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.loader.SingleTypeExternalContentProcessor;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.block.BlockConfig;
import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroup;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.UUID;

import javax.inject.Inject;

public final class SoundBlockContentProcessor implements BlockContentProcessor.State.Any {

    private static final ConfigTag TAG = ConfigTag.create(BlockConfig.State.SOUND);
    private final SingleTypeExternalContentProcessor<BlockSoundGroup, BlockSoundGroup.Builder> bsg;

    @Inject
    private SoundBlockContentProcessor(final SingleTypeExternalContentProcessor<BlockSoundGroup, BlockSoundGroup.Builder> bsg) {
        this.bsg = bsg;
    }

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(final ConfigurationNode config, final ContentBlockType.Builder<ContentBlockType, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder, final BlockStateDefinition.Builder<BlockStateDefinition> definition) {
        if (config.getValue() instanceof String) {
            definition.sound(CatalogDelegate.namespaced(BlockSoundGroup.class, config));
        } else {
            final BlockSoundGroup.Builder bsg = this.bsg.processExternal(builder.string(ContentBuilder.StringType.NAMESPACE), config, UUID.randomUUID().toString());
            definition.sound(Delegate.supplying(bsg::build));
        }
    }
}
